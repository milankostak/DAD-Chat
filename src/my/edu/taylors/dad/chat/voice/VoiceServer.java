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
 * </ul>
 */
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
				System.out.print("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
				try {
					byte aData[] = receivePacket.getData();
					System.out.println(" order: "+aData[0]);
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