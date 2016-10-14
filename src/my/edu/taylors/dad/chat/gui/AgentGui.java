package my.edu.taylors.dad.chat.gui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import my.edu.taylors.dad.chat.entity.ClientType;

public class AgentGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int otherSideId;	

	public AgentGui(Socket socket, String title, int otherSideId) {
		super(title, ClientType.AGENT);
		this.socket = socket;
		this.otherSideId = otherSideId;
		setupWriter();
	}

	@Override
	public void sendMessage(String message) {
		writer.println(otherSideId);
		writer.println(message);
		writer.flush();
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
	protected void logOut() {
		// TODO Auto-generated method stub
		
	}

}
