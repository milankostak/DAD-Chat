package my.edu.taylors.dad.chat.gui;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;

public class AgentGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int clientId;
	
	private String customerName, agentName;

	public AgentGui(Socket socket, String customerName, int clientId, String agentName) {
		super("Agent: conversation with customer " + customerName, ClientType.AGENT);
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
			writer.flush();
		}
	}

	@Override
	protected void setupWriter() {
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void invokeLogOut() {
		setLoggingOut(true);
		writer.println(Flags.LOGOUT);
		writer.println(clientId);
		writer.flush();
		logOut(new Message("Agent ended the conversation", ClientType.ME));
	}

	@Override
	public void logOut(Message message) {
		setLoggingOut(true);
		addMessage(message);
		disableControls();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		saveConversation();
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void saveConversation() {
		List<Message> messages = getChatListModel().getMessages();
				long timestamp = new Date().getTime();
		File file = new File("logs/" + timestamp + ".txt");
		file.getParentFile().mkdirs();

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			writer.println("Created on " + new Date());
			writer.println();
			for (int i = 0; i < messages.size(); i++) {
				Message msg = messages.get(i);
				String who = msg.getClientType() == ClientType.ME ? "Agent " + agentName : "Customer " + customerName;
				writer.println(who + ": "  + msg.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
	}

}
