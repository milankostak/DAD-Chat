package my.edu.taylors.dad.chat.client;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Agent;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.Ports;
import my.edu.taylors.dad.chat.gui.AgentGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;
import my.edu.taylors.dad.chat.voice.VoiceServer;

/**
 * Class of agent client<br>
 * Its thread handles customer to agent communication (it receives)
 */
public class ClientAgent extends Thread {

	private JFrame waitingWindow;
	private Socket socket;
	private Agent agent;
	private VoiceServer voiceServer;

	// holds references to both agent's windows
	public static Map<Integer, AgentGui> windows = new HashMap<>(2);

	public ClientAgent(Socket socket, Agent agent) {
		this.socket = socket;
		this.agent = agent;
		setupWaitingGui();
		voiceServer = new VoiceServer(Ports.VOICE_SERVER_AGENT, agent.getInetAddress(), agent.getMulticastAddress());
		start();
	}

	private void setupWaitingGui() {
		waitingWindow = new WaitingWindow(" Please wait for a customer to connect.");
		waitingWindow.setVisible(true);
	}

	/**
	 * Sends text message to both customers
	 * @param message text of message
	 */
	public static void sendBoth(String message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.showMessage(message);
		}
	}

	/**
	 * Sends voice message to both customers
	 * @param message {@link Message}
	 */
	public static void sendVoiceBoth(Message message) {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.addMessage(message);
			gui.sendVoiceFinished();
		}
	}

	/**
	 * Send clear command to customers to clear their buffers so there is no overlap
	 */
	public static void sendClear() {
		for (Map.Entry<Integer, AgentGui> entry : windows.entrySet()) {
			AgentGui gui = entry.getValue();
			gui.sendClear();
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

				// if received this flag, next data coming is new customer
				if (message.equals(Flags.SENDING_CUSTOMER_TO_AGENT)) {
					getNewCustomer();

				// customer logged out, confirm back to server to break loop
				} else if (message.equals(Flags.CUSTOMER_LOGGING_OUT)) {
					String customerId = fromServer.readLine();
					AgentGui agentGui = windows.get(Integer.parseInt(customerId));
					agentGui.getWriter().println(Flags.CUSTOMER_LOGGING_OUT);
					agentGui.getWriter().println(customerId);
					prepareLogOut(customerId);

				// customer finished voice capture, pick up buffer and show message
				} else if (message.equals(Flags.VOICE_CAPTURE_FINISHED)) {
					String customerId = fromServer.readLine();
					AgentGui agentGui = windows.get(Integer.parseInt(customerId));
					byte[] voiceData = voiceServer.getByteOutputStream();
					Message msg = new Message(voiceData, ClientType.NOT_ME);
					if (agentGui != null) {
						agentGui.addMessage(msg);
					}

				// otherwise receive normal text message
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

	/**
	 * Customer logged out, perform necessary actions
	 * @param customerId string with customer id
	 */
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

	/**
	 * After log out is complete, remove window from HashMap<br>
	 * If it is empty then, show waiting dialog
	 * @param customerIdInt number with customer id
	 */
	public void removeWindow(int customerIdInt) {
		windows.remove(customerIdInt);
		if (windows.size() == 0) {
			waitingWindow.setVisible(true);
		}
	}

	/**
	 * When received flag for receiving new customer, this is the method that handles it<br>
	 * After that it creates a new window for communication with that customer
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void getNewCustomer() throws IOException, ClassNotFoundException {

		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		writer.println(Flags.SENDING_CUSTOMER_TO_AGENT);

		ObjectInputStream ios = new ObjectInputStream(socket.getInputStream());
		Customer customer = (Customer) ios.readObject();
		System.out.println("Agent received customer: " + customer.toString());

		int clientId = customer.getId();
		int windowId = customer.getWindowId();

		AgentGui gui = new AgentGui(this, socket, customer.getUsername(), clientId, agent.getUsername(),
				agent.getInetAddress(), agent.getMulticastAddress());
		windows.put(windowId, gui);

		gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.ME));

		waitingWindow.setVisible(false);
	}

}
