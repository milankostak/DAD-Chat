package my.edu.taylors.dad.chat.gsa;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import my.edu.taylors.dad.chat.entity.Ports;

/**
 * Keep sending address to every client that wants it
 */
public class GsaServer extends Thread {

	public GsaServer() {
		start();
	}
	
	@Override
	public void run() {
		DatagramSocket dgSocket = null;
		try {
			dgSocket = new DatagramSocket(Ports.GSA_SERVER);

			while(true) {
				byte[] buffer = new byte[1];
				
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				dgSocket.receive(packet);
				
				Socket socket = null;
				try {
					socket = new Socket(packet.getAddress(), Ports.GSA_CLIENT);
				} catch (ConnectException e) {
					System.err.println("ConnectException");
					System.err.println("Client already received server address through other its interface.");
				} finally {
					if (socket != null) socket.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dgSocket != null) dgSocket.close();
		}
	}

}
