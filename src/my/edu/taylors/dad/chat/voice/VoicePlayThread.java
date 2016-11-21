package my.edu.taylors.dad.chat.voice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * For source see {@link VoiceServer}
 */
public class VoicePlayThread extends Thread {

	private AudioInputStream inputStream;
	private SourceDataLine sourceLine;

	public VoicePlayThread(byte[] voiceArray) {
		setVariables(voiceArray);
		start();
	}

	public void run() {
		byte tempBuffer[] = new byte[3000];
		
		try {
			int cnt;
			while ((cnt = inputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
				if (cnt > 0) {
					sourceLine.write(tempBuffer, 0, cnt);
				}
			}
			// sourceLine.drain();
			// sourceLine.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void setVariables(byte[] voiceArray) {
		try {
			InputStream byteInputStream = new ByteArrayInputStream(voiceArray);

			AudioFormat audioFormat = VoiceUtils.getAudioFormat();
			inputStream = new AudioInputStream(byteInputStream, audioFormat, voiceArray.length / audioFormat.getFrameSize());
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

			sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceLine.open(audioFormat);
			sourceLine.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

}
