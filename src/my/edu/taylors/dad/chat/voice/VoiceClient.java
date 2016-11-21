package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import my.edu.taylors.dad.chat.entity.Ports;

/**
 * For source see {@link VoiceServer}
 */
public class VoiceClient {

	private volatile boolean isCaptureRunning;
	private volatile ByteArrayOutputStream byteOutputStream;
	private TargetDataLine targetDataLine;
	private final InetAddress inetAddress;
	private final int serverPort;

	public VoiceClient(InetAddress InetAddress, int serverPort) {
		this.inetAddress = InetAddress;
		this.isCaptureRunning = false;
		this.serverPort = serverPort;
	}
	
	public byte[] stopCapture() {
		isCaptureRunning = false;
		targetDataLine.close();
		return byteOutputStream.toByteArray();
	}

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

	private class VoiceCaptureThread extends Thread {

		public VoiceCaptureThread() {
			start();
		}

		public void run() {
			byte[] tempBuffer = new byte[3000];

			byteOutputStream = new ByteArrayOutputStream();
			isCaptureRunning = true;

			try (DatagramSocket clientSocket = new DatagramSocket(Ports.VOICE_CLIENT)) {

				while (isCaptureRunning) {
					int count = targetDataLine.read(tempBuffer, 0, tempBuffer.length);

					if (count > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, inetAddress, serverPort);
						clientSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, count);
					}
				}
				byteOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
