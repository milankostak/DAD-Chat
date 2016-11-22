package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * For source see {@link VoiceServer}<br>
 * This class handles recording voice and sending it to give multicast address thru given interface
 */
public class VoiceClient {

	private volatile boolean isCaptureRunning;
	private volatile ByteArrayOutputStream byteOutputStream;
	private TargetDataLine targetDataLine;

	private final int serverPort;
	private final InetAddress interfaceAddress;
	private final String multicastAddress;

	public VoiceClient(int serverPort, InetAddress interfaceAddress, String multicastAddress) {
		this.isCaptureRunning = false;
		this.serverPort = serverPort;
		this.interfaceAddress = interfaceAddress;
		this.multicastAddress = multicastAddress;
	}

	/**
	 * Stop capturing and return captured voice data as byte array<br>
	 * Brakes recording loop
	 * @return captured voice data
	 */
	public byte[] stopCapture() {
		isCaptureRunning = false;
		targetDataLine.close();
		return byteOutputStream.toByteArray();
	}

	/**
	 * Start capturing voice<br>
	 * Prepare necessary objects and run a {@link VoiceCaptureThread}
	 */
	public void captureAudio() {
		if (isCaptureRunning) return;
		try {
			AudioFormat adFormat = VoiceUtils.getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);

			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(adFormat);
			targetDataLine.start();
	
			new VoiceCaptureThread();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thread for actual capturing of voice<br>
	 * Keeps sending it and saving to ByteArrayOutputStream at the same time for future replay
	 */
	private class VoiceCaptureThread extends Thread {

		public VoiceCaptureThread() {
			start();
		}

		public void run() {
			byte[] tempBuffer = new byte[VoiceUtils.PACKET_SIZE];

			byteOutputStream = new ByteArrayOutputStream();
			isCaptureRunning = true;

			try (MulticastSocket multicastSocket = new MulticastSocket()) {

				multicastSocket.setInterface(interfaceAddress);
				InetAddress multicastInetAdd = InetAddress.getByName(multicastAddress);

				// stopped in stopCapture() method
				while (isCaptureRunning) {
					int count = targetDataLine.read(tempBuffer, 0, tempBuffer.length);

					if (count > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, multicastInetAdd, serverPort);
						multicastSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, count);
					}

				}
				byteOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
