package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class VoiceServer {

	public static void main(String args[]) {
		new VoiceServer();
	}

	public VoiceServer() {
		try (DatagramSocket serverSocket = new DatagramSocket(9786)) { 
			byte[] receiveData = new byte[10000];
			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
				try {
					byte aData[] = receivePacket.getData();
					InputStream byteInputStream = new ByteArrayInputStream(aData);

					AudioFormat aFormat = VoiceUtils.getAudioFormat();
					AudioInputStream inputStream = new AudioInputStream(byteInputStream, aFormat, aData.length / aFormat.getFrameSize());
					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, aFormat);

					SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					sourceLine.open(aFormat);
					sourceLine.start();

					new VoicePlayThread(inputStream, sourceLine);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
