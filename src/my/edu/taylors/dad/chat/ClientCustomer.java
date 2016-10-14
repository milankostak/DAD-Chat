package my.edu.taylors.dad.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
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
			Auth agent = (Auth) ois.readObject();
			int agentWindowId = agent.getId();
			waitingWindow.setVisible(false);
			// TODO agent.getusername()
			new CustomerGui(socket, "Customer: " + authCustomer.getUsername(), agentWindowId);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
