package my.edu.taylors.dad.chat.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public class CustomerGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private boolean keepReceiving;
	private int otherSideId;	

	public CustomerGui(Socket socket, String title, int otherSideId) {
		super(title, ClientType.CUSTOMER);
		this.socket = socket;
		this.otherSideId = otherSideId;
		setupWriter();
		setReceivingThread();
	}

	@Override
	protected void sendMessage(String message) {
		writer.println(otherSideId);
		writer.println(message);
		writer.flush();
	}

	private void setReceivingThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					keepReceiving = true;
					while (keepReceiving) {
						String message = fromServer.readLine();
						Message msg = new Message(new Date(), message, ClientType.NOT_ME);
						addMessage(msg);
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
	protected void setupWriter() {
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
