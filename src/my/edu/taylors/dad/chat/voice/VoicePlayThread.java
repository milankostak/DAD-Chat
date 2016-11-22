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
 * For source see {@link VoiceServer}<br>
 * Thread for playing of either captured or received sound
 */
public class VoicePlayThread extends Thread {

	private AudioInputStream inputStream;
	private SourceDataLine sourceLine;
	private volatile boolean stopPlaying;

	/**
	 * Basic constructor which needs data to play as parameter
	 * @param voiceArray
	 */
	public VoicePlayThread(byte[] voiceArray) {
		setVariables(voiceArray);
		stopPlaying = false;
		start();
	}

	@Override
	public void run() {
		byte tempBuffer[] = new byte[1000];

		try {
			int count;
			while (!stopPlaying && (count = inputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
				if (count > 0) {
					sourceLine.write(tempBuffer, 0, count);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStopPlaying(boolean stopPlaying) {
		this.stopPlaying = stopPlaying;
	}

	/**
	 * Create necessary objects, after that playing is started
	 * @param voiceArray
	 */
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
