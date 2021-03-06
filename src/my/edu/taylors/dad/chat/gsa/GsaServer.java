package my.edu.taylors.dad.chat.gsa;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import my.edu.taylors.dad.chat.entity.Ports;

/**
 * Keep sending address to every client that asks for it
 */
public class GsaServer extends Thread {

	public GsaServer() {
		start();
	}

	@Override
	public void run() {
		try (DatagramSocket dgSocket = new DatagramSocket(Ports.GSA_SERVER)) {

			while(true) {
				byte[] buffer = new byte[1];

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				dgSocket.receive(packet);

				try (Socket socket = new Socket(packet.getAddress(), Ports.GSA_CLIENT)) {

				} catch (ConnectException e) {
					System.err.println("ConnectException");
					System.err.println("Client already received server address through other its interface.");
				}
			}

		} catch (BindException e) {
			System.err.println("There is already a server running on this port. Server is shuting down.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
