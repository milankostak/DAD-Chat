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

	private boolean isCaptureRunning = false;
	private ByteArrayOutputStream byteOutputStream; // for future replay
	private TargetDataLine targetDataLine;
	private final InetAddress inetAddress;

	public VoiceClient(InetAddress InetAddress) {
		this.inetAddress = InetAddress;
	}
	
	public ByteArrayOutputStream stopCapture() {
		isCaptureRunning = false;
		targetDataLine.close();
		return byteOutputStream;
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
	
		byte tempBuffer[] = new byte[10000];

		public VoiceCaptureThread() {
			start();
		}

		public void run() {
			//int order = 0;

			byteOutputStream = new ByteArrayOutputStream();
			isCaptureRunning = true;
			try (DatagramSocket clientSocket = new DatagramSocket(Ports.VOICE_CLIENT)) {
				while (isCaptureRunning) {
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					//tempBuffer[0] = (byte) order++;
					if (cnt > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, inetAddress, Ports.VOICE_SERVER);
						clientSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, cnt);
					}
				}
				byteOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
