package my.edu.taylors.dad.chat.gui;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.Ports;

/**
 * Implements {@link ChatWindow} for customer
 */
public class CustomerGui extends ChatWindow {
	private static final long serialVersionUID = 1L;

	// communication components
	private Socket socket;
	private PrintWriter writer;
	private int otherSideId;

	public CustomerGui(Socket socket, String title, int otherSideId,
			InetAddress customerAddress, String multicastAddress) {
		super(title, ClientType.CUSTOMER, Ports.VOICE_SERVER_AGENT, customerAddress, multicastAddress);
		this.socket = socket;
		this.otherSideId = otherSideId;
		setupWriter();
	}

	@Override
	protected void sendMessage(String message) {
		if (!isLoggingOut()) {
			writer.println(otherSideId);
			writer.println(message);
		}
	}

	@Override
	protected void sendVoiceFinished() {
		if (!isLoggingOut()) {
			writer.println(Flags.VOICE_CAPTURE_FINISHED);
			writer.println(otherSideId);
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
		writer.println(Flags.CUSTOMER_LOGGING_OUT);
		writer.println(otherSideId);
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
			//Close after logging out
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PrintWriter getWriter() {
		return writer;
	}

}
