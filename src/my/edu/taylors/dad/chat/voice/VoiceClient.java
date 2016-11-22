package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * For source see {@link VoiceServer}
 */
public class VoiceClient {

	private volatile boolean isCaptureRunning;
	private volatile ByteArrayOutputStream byteOutputStream;
	private TargetDataLine targetDataLine;

	private final int serverPort;

	public VoiceClient(InetAddress inetAddress, int serverPort) {
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
			byte[] tempBuffer = new byte[VoiceUtils.PACKET_SIZE];

			byteOutputStream = new ByteArrayOutputStream();
			isCaptureRunning = true;

			try (MulticastSocket multicastSocket = new MulticastSocket();) {

				multicastSocket.setInterface(InetAddress.getByName("192.168.137.207"));
				InetAddress multicastgroupAddress = InetAddress.getByName("235.1.1.1");
				
				while (isCaptureRunning) {
					int count = targetDataLine.read(tempBuffer, 0, tempBuffer.length);

					if (count > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length,
								multicastgroupAddress, serverPort);
						multicastSocket.send(sendPacket);
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
