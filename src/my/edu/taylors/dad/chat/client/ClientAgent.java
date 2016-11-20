package my.edu.taylors.dad.chat.client;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthIdIp;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.gui.AgentGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;
import my.edu.taylors.dad.chat.voice.VoiceClient;
import my.edu.taylors.dad.chat.voice.VoiceServer;

public class ClientAgent extends Thread {

	private JFrame waitingFrame;
	public static Map<Integer, AgentGui> windows = new HashMap<>(2);

	private Auth agent;
	private InetAddress customerIp;
	private Socket socket;

	public ClientAgent(Socket socket, Auth agent) {
		this.socket = socket;
		this.agent = agent;
		setupWaitingGui();
		start();
		new VoiceServer();
	}

	private void setupWaitingGui() {		
		waitingFrame = new WaitingWindow(" Please wait for a client to connect.");
		waitingFrame.setVisible(true);
	}

	public static void sendBoth(String message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.showMessage(message);
		}
	}

	@Override
	public void run() {
		try {
			// customer to agent
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			boolean keepReceiving = true;
			while (keepReceiving) {
				String message = fromServer.readLine();

				if (message.equals(Flags.SENDING_CUSTOMER_TO_AGENT)) {
					getNewCustomer();

				// customer logged out, confirm back to server to break loop
				} else if (message.equals(Flags.CUSTOMER_LOGGING_OUT)) {
					String customerId = fromServer.readLine();
					AgentGui agentGui = windows.get(Integer.parseInt(customerId));
					agentGui.getWriter().println(Flags.CUSTOMER_LOGGING_OUT);
					agentGui.getWriter().println(customerId);
					prepareLogOut(customerId);

 				} else {
 					int customerId = Integer.parseInt(message);
					String messageFromAgent = fromServer.readLine();
					Message msg = new Message(messageFromAgent, ClientType.NOT_ME);
					AgentGui agentGui = windows.get(customerId);
					if (agentGui != null) {
						agentGui.addMessage(msg);
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void prepareLogOut(String customerId) {
		int customerIdInt = Integer.parseInt(customerId);
		AgentGui agentGui = windows.get(customerIdInt);
		if (agentGui != null) {
			agentGui.logOut(new Message("Customer ended conversation", ClientType.NOT_ME));
			agentGui.dispatchEvent(new WindowEvent(agentGui, WindowEvent.WINDOW_CLOSING));
			agentGui.dispose();
			agentGui = null;
		}
	}

	public void removeWindow(int customerIdInt) {
		windows.remove(customerIdInt);
		if (windows.size() == 0) {
			waitingFrame.setVisible(true);
		}
	}

	private void getNewCustomer() throws IOException, ClassNotFoundException {

		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		writer.println(Flags.SENDING_CUSTOMER_TO_AGENT);
		
		ObjectInputStream ios = new ObjectInputStream(socket.getInputStream());
		AuthIdIp customer = (AuthIdIp) ios.readObject();
		System.out.println("Agent received customer: " + customer.toString());

		int clientId = customer.getId();
		int windowId = customer.getWindowId();
		customerIp = customer.getInetAddress();
		new VoiceClient(customerIp);

		AgentGui gui = new AgentGui(this, socket, customer.getUsername(), clientId, agent.getUsername());
		windows.put(windowId, gui);

		gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.ME));
		
		waitingFrame.setVisible(false);
	}
}
