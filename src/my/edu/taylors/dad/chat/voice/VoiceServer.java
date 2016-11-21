package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
 * 	<li>VoiceServe extends a Thread now</li>
 * 	<li>Completely removed GUI</li>
 * 	<li>Server now doesn't play immediately, but stores the sound for later play</li>
 * 	<li>Memory synchronization (volatile) </li>
 * </ul>
 */
public class VoiceServer extends Thread {

	private volatile ByteArrayOutputStream byteOutputStream;
	private volatile boolean cleared;
	private final int serverPort;

	public VoiceServer(int serverPort) {
		this.cleared = true;
		this.serverPort = serverPort;
		start();
	}

	public byte[] getByteOutputStream() {
		cleared = true;
		return byteOutputStream.toByteArray();
	}

	@Override
	public void run() {
		try (DatagramSocket serverSocket = new DatagramSocket(serverPort)) {

			byte[] receiveData = new byte[3000];

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
