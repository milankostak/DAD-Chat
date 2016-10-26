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

/**
 * Send broadcasts to every interface that is up and wait for response
 * Than get address from this response - it is the server address
 */
public class GsaClient extends Thread {

	public GsaClient() { }
	
	public String getAddress() {
		ServerSocket serverConn = null;
		String serverAddress = null;
		try {
			try {
				serverConn = new ServerSocket(6869);
				start();// start thread for sending requests
				// then wait for reply
				Socket socket = serverConn.accept();
				
				serverAddress = socket.getInetAddress().getHostAddress();
				System.out.println("Received server address: " + serverAddress);

			} finally {
				serverConn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return serverAddress;
	}
	
	@Override
	public void run() {
		try {
			List<InetAddress> broadcastAddresss = new ArrayList<>();
	
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	
			while (interfaces.hasMoreElements()) {
				// hw interface
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendBroadcast(InetAddress broadcastAddress) throws IOException {
		byte[] buffer = {0};
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, 6868);
		DatagramSocket dgSocket = new DatagramSocket();
		dgSocket.send(packet);
		dgSocket.close();
	}

}
