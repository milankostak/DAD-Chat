package my.edu.taylors.dad.chat.voice;

import javax.sound.sampled.AudioFormat;

/**
 * For source see {@link VoiceServer}
 */
public class VoiceUtils {
	
	public static final int PACKET_SIZE = 1000; 

	private VoiceUtils() { }

	public static AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		int sampleInbits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
	}

}
