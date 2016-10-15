package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthWithWindowId;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.gui.AgentGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;

public class ClientAgent extends Thread {

	private JFrame waitingFrame;
	private static Map<Integer, AgentGui> windows = new HashMap<>(2);
	private boolean keepReceiving;
	private Auth agent;

	private Socket socket;

	public ClientAgent(Socket socket, Auth agent) {
		this.socket = socket;
		this.agent = agent;
		setupWaitingGui();
		start();
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
			keepReceiving = true;
			while (keepReceiving) {
				String id = fromServer.readLine();
				if (id.equals("-3")) {
					getNewCustomer();
				} else {
					String message = fromServer.readLine();
					Message msg = new Message(message, ClientType.NOT_ME);
					AgentGui agentGui = windows.get(Integer.parseInt(id));
					if (agentGui != null) {
						agentGui.addMessage(msg);
					}
				}
			}
		} catch (SocketException e) {
			// throws when closing window, because it is waiting for server while we close the socket
			e.printStackTrace();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getNewCustomer() throws IOException, ClassNotFoundException {

		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		writer.println("-3");
		
		ObjectInputStream ios = new ObjectInputStream(socket.getInputStream());
		AuthWithWindowId customer = (AuthWithWindowId) ios.readObject();
		System.out.println("Agent received customer: " + customer.toString());
		//TODO StreamCorruptedException when written two customers at server side

		int clientId = customer.getId();
		int windowId = customer.getWindowId();

		AgentGui gui = new AgentGui(socket, "Agent: " + customer.getUsername(), clientId);
		windows.put(windowId, gui);

		gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.ME));
		
		waitingFrame.setVisible(false);
	}
}
