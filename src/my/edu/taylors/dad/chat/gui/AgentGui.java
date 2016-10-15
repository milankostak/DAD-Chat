package my.edu.taylors.dad.chat.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public class AgentGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int otherSideId;
	private boolean isLoggingOut;

	public AgentGui(Socket socket, String title, int otherSideId) {
		super(title, ClientType.AGENT);
		this.socket = socket;
		this.otherSideId = otherSideId;
		this.isLoggingOut = false;
		setupWriter();
	}

	@Override
	public void sendMessage(String message) {
		if (!isLoggingOut) {
			writer.println(otherSideId);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logOut() {
		isLoggingOut = true;
		disableControls();
		// TODO Auto-generated method stub

	}

	public void saveConversation() {
		List<Message> messages = getChatListModel().getMessages();
		long timestamp = new Date().getTime();
		File file = new File("logs/" + timestamp + ".txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			for (int i = 0; i < messages.size(); i++) {
				Message msg = messages.get(i);
				String who = msg.getClientType() == ClientType.ME ? "Agent" : "Customer";
				writer.println(who + ":"  + msg.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
	}

}
