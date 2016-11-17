package my.edu.taylors.dad.chat.voice;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

public class VoicePlayThread extends Thread {

	byte tempBuffer[] = new byte[10000];

	private AudioInputStream inputStream;
	private SourceDataLine sourceLine;

	public VoicePlayThread(AudioInputStream inputStream, SourceDataLine sourceLine) {
		this.inputStream = inputStream;
		this.sourceLine = sourceLine;
		start();
	}

	public void run() {
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
}