package my.edu.taylors.dad.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JFrame;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.gui.ClientGui;

public class ClientAgent extends Thread {
	
	private JFrame waitingFrame;

	private Socket socket;

	public ClientAgent(Socket socket) {
		this.socket = socket;
		setupWaitingGui();
		start();
	}
	
	private void setupWaitingGui() {
		// TODO Auto-generated method stub
	}

	@Override
	public void run() {

		try {
			Socket connectingSocket = null;
			try {
				connectingSocket = new Socket("127.0.0.1", 9998);
				while (true) {
					// get customer info
					ObjectInputStream customer = new ObjectInputStream(connectingSocket.getInputStream());
					Auth auth = (Auth) customer.readObject();
					int id = auth.getId();

					new ClientGui(socket, "Agent: " + auth.getUsername(), id);
					System.out.println(auth.getUsername());
				}
			} finally {
				if (connectingSocket != null) connectingSocket.close();
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}