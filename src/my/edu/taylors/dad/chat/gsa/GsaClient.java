package my.edu.taylors.dad.chat.gsa;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;

import my.edu.taylors.dad.chat.entity.Ports;

/**
 * Send broadcasts to every interface that is up and wait for response
 * Than get address from this response - it is the server address
 */
public class GsaClient extends Thread {

	private volatile boolean addressReceived = false;

	public String getAddress() {
		String serverAddress = null;
		try {
			try (ServerSocket serverConn = new ServerSocket(Ports.GSA_CLIENT)) {

				start();// start thread for sending requests
				// then wait for reply
				Socket socket = serverConn.accept();

				serverAddress = socket.getInetAddress().getHostAddress();
				System.out.println("Received server address: " + serverAddress);
				addressReceived = true;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return serverAddress;
	}

	@Override
	public void run() {
		int counter = 0;
		int limit = 5;
		while (!addressReceived) {
			try {
				List<InetAddress> broadcastAddresss = new ArrayList<>();

				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

				while (interfaces.hasMoreElements()) {
					// HW interface
					NetworkInterface interf = (NetworkInterface) interfaces.nextElement();
					if (interf.isUp()) {

						for (InterfaceAddress address : interf.getInterfaceAddresses()) {
							if (address.getBroadcast() != null) {
								broadcastAddresss.add(address.getBroadcast());
							}
						}
		
					}
				}

				for (int i = 0; i < broadcastAddresss.size(); i++) {
					sendBroadcast(broadcastAddresss.get(i));
				}

				if (++counter > limit) {
					showFail();
				} else {
					// wait 2 seconds for server address, if not received send requests again
					Thread.sleep(2000);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendBroadcast(InetAddress broadcastAddress) throws IOException {
		if (broadcastAddress.toString().equals("/127.255.255.255") || broadcastAddress.toString().equals("/169.254.255.255")) return;
		byte[] buffer = {0};
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, Ports.GSA_SERVER);
		DatagramSocket dgSocket = new DatagramSocket();
		dgSocket.send(packet);
		dgSocket.close();
	}
	
	private void showFail() {
		JOptionPane.showMessageDialog(
					null,
					"Client was unable to find server.\nPlease try again later.",
					"Server not found",
					JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}

}
