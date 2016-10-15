package my.edu.taylors.dad.chat.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;

public class CustomerGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int otherSideId;

	public CustomerGui(Socket socket, String title, int otherSideId) {
		super(title, ClientType.CUSTOMER);
		this.socket = socket;
		this.otherSideId = otherSideId;
		setupWriter();
		setReceivingThread().start();
	}

	@Override
	protected void sendMessage(String message) {
		if (!isLoggingOut()) {
			writer.println(otherSideId);
			writer.println(message);
			writer.flush();
		}
	}

	private Thread setReceivingThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					while (!isLoggingOut()) {
						String flag = fromServer.readLine();
						if (flag != null && flag.equals(Flags.LOGOUT)) {
							logOut(new Message("Agent ended the conversation", ClientType.NOT_ME));
						} else {
							String message = fromServer.readLine();
							Message msg = new Message(message, ClientType.NOT_ME);
							addMessage(msg);
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
		return thread;
	}
	
	@Override
	protected void setupWriter() {
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void invokeLogOut() {
		setLoggingOut(true);
		writer.println(Flags.LOGOUT);
		writer.println(otherSideId);
		writer.flush();
		logOut(new Message("Customer ended the conversation", ClientType.ME));
	}

	@Override
	public void logOut(Message message) {
		setLoggingOut(true);
		addMessage(message);
		disableControls();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
