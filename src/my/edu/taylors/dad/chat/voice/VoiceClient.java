package my.edu.taylors.dad.chat.voice;

import java.awt.Color;
import java.awt.FlowLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * For source see {@link VoiceServer}
 */
public class VoiceClient extends JFrame {
	private static final long serialVersionUID = 1L;

	private boolean stopAudioCapture = false;
	private ByteArrayOutputStream byteOutputStream; // for future replay
	private TargetDataLine targetDataLine;

	public static void main(String args[]) {
		new VoiceClient().setVisible(true);
	}

	public VoiceClient() {
		final JButton capture = new JButton("Capture");
		final JButton stop = new JButton("Stop");
		final JButton play = new JButton("Playback");

		capture.setEnabled(true);
		stop.setEnabled(false);
		play.setEnabled(false);

		// init capture button
		capture.addActionListener(e -> {
			capture.setEnabled(false);
			stop.setEnabled(true);
			play.setEnabled(false);
			captureAudio();
		});
		getContentPane().add(capture);

		// init stop button
		stop.addActionListener(e -> {
			capture.setEnabled(true);
			stop.setEnabled(false);
			play.setEnabled(true);
			stopAudioCapture = true;
			targetDataLine.close();
		});
		getContentPane().add(stop);

		// init play button
		play.addActionListener(e -> playAudio());
		getContentPane().add(play);

		getContentPane().setLayout(new FlowLayout());
		setTitle("Capture/Playback Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 100);
		getContentPane().setBackground(Color.WHITE);
	}

	private void captureAudio() {
		try {
			AudioFormat adFormat = VoiceUtils.getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);

			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(adFormat);
			targetDataLine.start();
	
			new VoiceCaptureThread();
		} catch (Exception e) {
			StackTraceElement stackEle[] = e.getStackTrace();
			for (StackTraceElement val : stackEle) {
				System.out.println(val);
			}
			System.exit(0);
		}
	}

	private void playAudio() {
		try {
			byte aData[] = byteOutputStream.toByteArray();
			InputStream byteInputStream = new ByteArrayInputStream(aData);

			AudioFormat aFormat = VoiceUtils.getAudioFormat();
			AudioInputStream inputStream = new AudioInputStream(byteInputStream, aFormat, aData.length / aFormat.getFrameSize());
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, aFormat);

			SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceLine.open(aFormat);
			sourceLine.start();

			new VoicePlayThread(inputStream, sourceLine);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}

	private class VoiceCaptureThread extends Thread {
	
		byte tempBuffer[] = new byte[10000];

		public VoiceCaptureThread() {
			start();
		}

		public void run() {
			int order = 0;

			byteOutputStream = new ByteArrayOutputStream();
			stopAudioCapture = false;
			try (DatagramSocket clientSocket = new DatagramSocket(8786)) {
				// TODO add IP address from LoginMenu
				InetAddress IPAddress = InetAddress.getByName("192.168.137.195");
				while (!stopAudioCapture) {
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					tempBuffer[0] = (byte) order++;
					if (cnt > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 9786);
						clientSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, cnt);
					}
				}
				byteOutputStream.close();
			} catch (Exception e) {
				System.out.println("CaptureThread::run()" + e);
				System.exit(0);
			}
		}
	}

}
