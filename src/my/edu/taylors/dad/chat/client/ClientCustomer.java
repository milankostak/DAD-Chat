package my.edu.taylors.dad.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Agent;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.Ports;
import my.edu.taylors.dad.chat.gui.CustomerGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;
import my.edu.taylors.dad.chat.voice.VoiceServer;

/**
 * Class of customer client<br>
 * Its thread handles agent to customer communication (it receives)
 */
public class ClientCustomer extends Thread {

	private JFrame waitingWindow;
	private Socket socket;
	private Customer customer;
	private VoiceServer voiceServer;

	private CustomerGui gui;

	public ClientCustomer(Socket socket, Customer customer) {
		this.socket = socket;
		this.customer = customer;
		setupWaitingGui();
		start();
	}

	private void setupWaitingGui() {
		waitingWindow = new WaitingWindow(" All our agents are fully assigned. Please wait.");
		waitingWindow.setVisible(true);
	}

	@Override
	public void run() {
		// firstly get agent information and show window...
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Agent agent = (Agent) ois.readObject();
			System.out.println("Customer received agent: " + agent.toString());

			voiceServer = new VoiceServer(Ports.VOICE_SERVER_CUSTOMER, customer.getInetAddress(), agent.getMulticastAddress());

			waitingWindow.setVisible(false);
			
			int agentWindowId = agent.getWindowId();
			InetAddress customerAddress = customer.getInetAddress();
			String multicastAddress = agent.getMulticastAddress();

			gui = new CustomerGui(socket, "Customer: " + customer.getUsername(), agentWindowId, customerAddress, multicastAddress);
			gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.NOT_ME));
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		// ... then keep receiving messages from agent
		// agent to customer way
		try {
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (!gui.isLoggingOut()) {

				String flag = fromServer.readLine();
				if (flag != null) {

					// agent logged out, customer sending back confirmation
					if (flag.equals(Flags.AGENT_LOGGING_OUT)) {
						gui.getWriter().println(Flags.AGENT_LOGGING_OUT);
						gui.getWriter().close();
						fromServer.close();
						gui.logOut(new Message("Agent ended the conversation", ClientType.NOT_ME));

					// customer logged out, this is confirmation
					} else if (flag.equals(Flags.CUSTOMER_LOGGING_OUT)) {
						gui.getWriter().close();
						fromServer.close();
						gui.logOut(new Message("Customer ended the conversation", ClientType.ME));

					// agent finished voice capture, pick up buffer and show message
					} else if (flag.equals(Flags.VOICE_CAPTURE_FINISHED)) {
						byte[] voiceData = voiceServer.getByteOutputStream();
						Message msg = new Message(voiceData, ClientType.NOT_ME);
						gui.addMessage(msg);

					// clean buffer if case received data were for second customer
					} else if (flag.equals(Flags.VOICE_CAPTURE_CLEAR)) {
						voiceServer.getByteOutputStream();

					} else {
						String message = fromServer.readLine();
						Message msg = new Message(message, ClientType.NOT_ME);
						gui.addMessage(msg);
					}

				} // if (flag != null)

			} // while (!gui.isLoggingOut())
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
