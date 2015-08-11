package distractionTypingTest;

import java.io.File;
import java.io.IOException;
import java.sql.Time;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class JavaSoundRecorder {
	// record duration, in milliseconds
	public static final long RECORD_TIME = 60000; // 1 minute

	// path of the wav file
	File wavFile;

	Time recordingTime;

	// format of audio file
	AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

	// the line from which audio data is captured
	TargetDataLine line;

	public JavaSoundRecorder(String path) {
		wavFile = new File(path);
	}
	/**
	 * Defines an audio format
	 */
	AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
				channels, signed, bigEndian);
		return format;
	}

	/**
	 * Captures the sound and record into a WAV file
	 */
	void start() {
		try {
			AudioFormat format = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			// checks if system supports the data line
			if (!AudioSystem.isLineSupported(info)) {
				System.exit(0);
			}
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start(); // start capturing


			AudioInputStream ais = new AudioInputStream(line);

			recordingTime = new Time(System.currentTimeMillis());
			// start recording
			AudioSystem.write(ais, fileType, wavFile);

		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Closes the target data line to finish capturing and recording
	 */
	void finish() {
		line.stop();
		line.close();
	}

	/**
	 * Entry to run the program
	 */
	public static void main(String[] args) {
		// final JavaSoundRecorder recorder = new JavaSoundRecorder();
		//
		// // creates a new thread that waits for a specified
		// // of time before stopping
		// Thread stopper = new Thread(new Runnable() {
		// public void run() {
		// try {
		// Thread.sleep(RECORD_TIME);
		// } catch (InterruptedException ex) {
		// ex.printStackTrace();
		// }
		// recorder.finish();
		// }
		// });
		//
		// stopper.start();
		//
		// // start recording
		// recorder.start();

	}
}