package my.edu.taylors.dad.chat.gui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.client.ClientAgent;
import my.edu.taylors.dad.chat.client.LoginMenu;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.Ports;
import my.edu.taylors.dad.chat.saving.ISaver;

public class AgentGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int clientId;
	private ClientAgent clientAgent;
	
	private String customerName, agentName;

	public AgentGui(ClientAgent clientAgent, Socket socket, String customerName, int clientId, String agentName,
			InetAddress agentAddress, String multicastAddress) {
		super("Agent: conversation with customer " + customerName, ClientType.AGENT, Ports.VOICE_SERVER_CUSTOMER,
				agentAddress, multicastAddress);
		this.clientAgent = clientAgent;
		this.socket = socket;
		this.customerName = customerName;
		this.clientId = clientId;
		this.agentName = agentName;
		setupWriter();
	}

	@Override
	public void sendMessage(String message) {
		if (!isLoggingOut()) {
			writer.println(clientId);
			writer.println(message);
		}
	}

	@Override
	public void sendVoiceFinished() {
		if (!isLoggingOut()) {
			writer.println(Flags.VOICE_CAPTURE_FINISHED);
			writer.println(clientId);
		}
	}

	public void sendClear() {
		if (!isLoggingOut()) {
			writer.println(Flags.VOICE_CAPTURE_CLEAR);
			writer.println(clientId);
		}
	}

	@Override
	protected void setupWriter() {
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void invokeLogOut() {
		writer.println(Flags.AGENT_LOGGING_OUT);
		writer.println(clientId);
		logOut(new Message("Agent ended the conversation", ClientType.ME));
	}

	@Override
	public void logOut(Message message) {
		setLoggingOut(true);
		addMessage(message);
		disableControls();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		saveConversation();
		clientAgent.removeWindow(clientId);
	}

	private void saveConversation() {
		try {
			Registry registry = LocateRegistry.getRegistry(LoginMenu.serverIpAddress);
			ISaver saver = (ISaver) registry.lookup("saverObject");

			saver.saveConversation(getChatListModel().getMessages(), agentName, customerName);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
	}

	public PrintWriter getWriter() {
		return writer;
	}

}
