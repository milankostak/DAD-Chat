package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Source: {@linkplain <a href="http://stackoverflow.com/revisions/17174202/2">http://stackoverflow.com/revisions/17174202/2</a>}<br>
 * Which is licensed under <a href="https://creativecommons.org/licenses/by-sa/3.0/">CC BY-SA 3.0</a><br>
 * <p>Major changes made:</p>
 * <ul>
 * 	<li>Removed unnecessary variables</li>
 * 	<li>Fields into local variables where possible</li>
 * 	<li>Separated Play Thread</li>
 * 	<li>Fixed closing leaks</li>
 * 	<li>Code formatted in big matter to improve readability</li>
 * 	<li>Improved exception handling</li>
 * 	<li>Listeners with lambdas</li>
 * 	<li>VoiceServer extends a Thread now</li>
 * 	<li>Completely removed GUI</li>
 * 	<li>Server now doesn't play immediately, but stores the sound for later play</li>
 * 	<li>Memory synchronization (volatile) </li>
 * </ul>
 */
public class VoiceServer extends Thread {

	private volatile ByteArrayOutputStream byteOutputStream;
	private volatile boolean cleared;

	private final int serverPort;
	private final InetAddress interfaceAddress;
	private final String multicastAddress;

	public VoiceServer(int serverPort, InetAddress interfaceAddress, String multicastAddress) {
		this.cleared = true;
		this.serverPort = serverPort;
		this.interfaceAddress = interfaceAddress;
		this.multicastAddress = multicastAddress;
		start();
	}

	public byte[] getByteOutputStream() {
		cleared = true;
		return byteOutputStream.toByteArray();
	}

	@Override
	public void run() {
		try (MulticastSocket serverSocket = new MulticastSocket(serverPort)) {

			InetAddress multicastgroupAddress = InetAddress.getByName(multicastAddress);
			serverSocket.setInterface(interfaceAddress);
			serverSocket.joinGroup(multicastgroupAddress);

			byte[] receiveData = new byte[VoiceUtils.PACKET_SIZE];

			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + ":" + receivePacket.getPort());
				
				if (cleared) {
					byteOutputStream = new ByteArrayOutputStream();
					cleared = false;
				}

				byte aData[] = receivePacket.getData();
				byteOutputStream.write(aData, 0, receivePacket.getLength());
			}
		} catch (BindException e) {
			System.err.println("BindException VoiceServer::run - Server is already running on this machine.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
