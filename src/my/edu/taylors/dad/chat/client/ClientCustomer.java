package my.edu.taylors.dad.chat.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthWithWindowId;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.gui.CustomerGui;
import my.edu.taylors.dad.chat.gui.WaitingWindow;

public class ClientCustomer extends Thread {
	
	private JFrame waitingWindow;
	private Socket socket;
	private Auth authCustomer;

	public ClientCustomer(Socket socket, Auth authCustomer) {
		this.socket = socket;
		this.authCustomer = authCustomer;
		setupWaitingGui();
		start();
	}
	
	private void setupWaitingGui() {
		waitingWindow = new WaitingWindow(" All our agents are fully assigned. Please wait.");
		waitingWindow.setVisible(true);
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			AuthWithWindowId agent = (AuthWithWindowId) ois.readObject();
			System.out.println("Customer received agent: " + agent.toString());
			int agentWindowId = agent.getWindowId();
			waitingWindow.setVisible(false);
			CustomerGui gui = new CustomerGui(socket, "Customer: " + authCustomer.getUsername(), agentWindowId);
			gui.addMessage(new Message("Hello, I am " + agent.getUsername() + ". How can I help you?", ClientType.NOT_ME));
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
