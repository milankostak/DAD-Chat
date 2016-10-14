package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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
		setReceivingThread();
		start();
	}

	private void setupWaitingGui() {		
		waitingFrame = new WaitingWindow(" please wait for a client to connect");
		waitingFrame.setVisible(true);
	}
	
	public static void sendBoth(String message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.showMessage(message);
		}
	}

	// every client receives just in one thread a forward the message to correct window
	private void setReceivingThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					keepReceiving = true;
					while (keepReceiving) {
						String id = fromServer.readLine();
						String message = fromServer.readLine();
						Message msg = new Message(message, ClientType.NOT_ME);
						AgentGui agentGui = windows.get(Integer.parseInt(id));
						if (agentGui != null) {
							agentGui.addMessage(msg);
						}
					}
				} catch (SocketException e) {
					// throws when closing window, because it is waiting for server while we close the socket
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	@Override
	public void run() {
		// this thread wait for server to tell that new customer was assigned to this agent
		try {
			Socket connectingSocket = null;
			try {
				connectingSocket = new Socket("127.0.0.1", 9998);
				while (true) {
					// get customer info
					//System.out.println("Agent receiving customer info");
					ObjectInputStream ios = new ObjectInputStream(connectingSocket.getInputStream());
					AuthWithWindowId customer = (AuthWithWindowId) ios.readObject();
					//TODO StreamCorruptedException when written two customers at server side
					//System.out.println("customer " + customer.toString());

					int clientId = customer.getId();
					int windowId = customer.getWindowId();

					AgentGui gui = new AgentGui(socket, "Agent: " + customer.getUsername(), clientId);
					windows.put(windowId, gui);

					gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.ME));
					
					waitingFrame.setVisible(false);
				}
			} finally {
				if (connectingSocket != null) connectingSocket.close();
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
