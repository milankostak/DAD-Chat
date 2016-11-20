package my.edu.taylors.dad.chat.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthIdIp;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.gui.CustomerGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;
import my.edu.taylors.dad.chat.voice.VoiceServer;

public class ClientCustomer extends Thread {

	private JFrame waitingWindow;
	private Socket socket;
	private Auth authCustomer;
	private VoiceServer voiceServer;

	private CustomerGui gui;

	public ClientCustomer(Socket socket, Auth authCustomer) {
		this.socket = socket;
		this.authCustomer = authCustomer;
		setupWaitingGui();
		voiceServer = new VoiceServer();
		start();
	}

	private void setupWaitingGui() {
		waitingWindow = new WaitingWindow(" All our agents are fully assigned. Please wait.");
		waitingWindow.setVisible(true);
	}

	@Override
	public void run() {
		try {// TODO StreamCorruptedException
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			AuthIdIp agent = (AuthIdIp) ois.readObject();
			System.out.println("Customer received agent: " + agent.toString());
			int agentWindowId = agent.getWindowId();
			InetAddress agentIp = agent.getInetAddress();

			waitingWindow.setVisible(false);
			gui = new CustomerGui(socket, "Customer: " + authCustomer.getUsername(), agentWindowId, agentIp);
			gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.NOT_ME));
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// receive from agent
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

					} else if (flag.equals(Flags.VOICE_CAPTURE_FINISHED)) {
						ByteArrayOutputStream voiceData = voiceServer.getByteOutputStream();
						Message msg = new Message(voiceData, ClientType.NOT_ME);
						gui.addMessage(msg);
						
					} else {
						String message = fromServer.readLine();
						Message msg = new Message(message, ClientType.NOT_ME);
						gui.addMessage(msg);
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
