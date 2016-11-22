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
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.Ports;
import my.edu.taylors.dad.chat.gui.AgentGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;
import my.edu.taylors.dad.chat.voice.VoiceServer;

public class ClientAgent extends Thread {

	private JFrame waitingWindow;
	private Socket socket;
	private Auth agent;
	private VoiceServer voiceServer;
	
	public static Map<Integer, AgentGui> windows = new HashMap<>(2);

	public ClientAgent(Socket socket, Auth agent) {
		this.socket = socket;
		this.agent = agent;
		setupWaitingGui();
		voiceServer = new VoiceServer(Ports.VOICE_SERVER_AGENT);
		start();
	}

	private void setupWaitingGui() {		
		waitingWindow = new WaitingWindow(" Please wait for a client to connect.");
		waitingWindow.setVisible(true);
	}

	public static void sendBoth(String message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.showMessage(message);
		}
	}

	public static void sendVoiceBoth(Message message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.addMessage(message);
			gui.sendVoiceFinished();
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

				} else if (message.equals(Flags.VOICE_CAPTURE_FINISHED)) {
					String customerId = fromServer.readLine();
					AgentGui agentGui = windows.get(Integer.parseInt(customerId));
					byte[] voiceData = voiceServer.getByteOutputStream();
					Message msg = new Message(voiceData, ClientType.NOT_ME);
					if (agentGui != null) {
						agentGui.addMessage(msg);
					}
					
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
			waitingWindow.setVisible(true);
		}
	}

	private void getNewCustomer() throws IOException, ClassNotFoundException {

		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		writer.println(Flags.SENDING_CUSTOMER_TO_AGENT);
		
		ObjectInputStream ios = new ObjectInputStream(socket.getInputStream());
		Customer customer = (Customer) ios.readObject();
		System.out.println("Agent received customer: " + customer.toString());

		int clientId = customer.getId();
		int windowId = customer.getWindowId();
		InetAddress customerIp = customer.getInetAddress();

		AgentGui gui = new AgentGui(this, socket, customer.getUsername(), clientId, agent.getUsername(), customerIp);
		windows.put(windowId, gui);

		gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.ME));
		
		waitingWindow.setVisible(false);
	}

}
