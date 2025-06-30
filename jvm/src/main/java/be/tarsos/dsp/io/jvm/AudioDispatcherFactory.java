/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package be.tarsos.dsp.io.jvm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;

public class AudioDispatcherFactory {

	public static AudioDispatcher fromDefaultMicrophone(final int audioBufferSize, final int bufferOverlap) throws LineUnavailableException {
		return fromDefaultMicrophone(44100, audioBufferSize, bufferOverlap);
	}
	
	public static AudioDispatcher fromDefaultMicrophone(final int sampleRate,final int audioBufferSize, final int bufferOverlap) throws LineUnavailableException {
		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,true);
		TargetDataLine line =  AudioSystem.getTargetDataLine(format);
		line.open(format, audioBufferSize);
		line.start();
		AudioInputStream stream = new AudioInputStream(line);
		TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
		return new AudioDispatcher(audioStream,audioBufferSize,bufferOverlap);
	}
	
	public static AudioDispatcher fromByteArray(final byte[] byteArray, final AudioFormat audioFormat,
			final int audioBufferSize, final int bufferOverlap) throws UnsupportedAudioFileException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		final long length = byteArray.length / audioFormat.getFrameSize();
		final AudioInputStream stream = new AudioInputStream(bais, audioFormat, length);
		TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
		return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
	}
	
	public static AudioDispatcher fromURL(final URL audioURL, final int audioBufferSize,final int bufferOverlap)
	throws UnsupportedAudioFileException, IOException {
		final AudioInputStream stream = AudioSystem.getAudioInputStream(audioURL);
		TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
		return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
	}
	
	public static AudioDispatcher fromPipe(final String source,final int targetSampleRate, final int audioBufferSize,final int bufferOverlap){
		return fromPipe(source, targetSampleRate, audioBufferSize, bufferOverlap,0);
	}
	
	public static AudioDispatcher fromPipe(final String source,final int targetSampleRate, final int audioBufferSize,final int bufferOverlap,final double startTimeOffset){
		if(source.startsWith("http") || (new File(source).exists() && new File(source).isFile() && new File(source).canRead())){
			PipedAudioStream f = new PipedAudioStream(source);
			TarsosDSPAudioInputStream audioStream = f.getMonoStream(targetSampleRate,startTimeOffset);
			return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
		}else{
			throw new IllegalArgumentException("The file " + source + " is not a readable file. Does it exist?");
		}
	}
	
	public static AudioDispatcher fromPipe(final String source,final int targetSampleRate, final int audioBufferSize,final int bufferOverlap,final double startTimeOffset,final double numberOfSeconds){
		if(new File(source).exists()&&new File(source).isFile() && new File(source).canRead()){
			PipedAudioStream f = new PipedAudioStream(source);
			TarsosDSPAudioInputStream audioStream = f.getMonoStream(targetSampleRate,startTimeOffset,numberOfSeconds);
			return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
		}else{
			throw new IllegalArgumentException("The file " + source + " is not a readable file. Does it exist?");
		}
	}
	
	public static AudioDispatcher fromFile(final File audioFile, final int audioBufferSize,final int bufferOverlap)
			throws UnsupportedAudioFileException, IOException {
		final AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
		TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
		return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
	}
	
	public static AudioDispatcher fromFloatArray(final float[] floatArray, final int sampleRate, final int audioBufferSize, final int bufferOverlap) throws UnsupportedAudioFileException {
		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
		final TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(JVMAudioInputStream.toTarsosDSPFormat(format));
		final byte[] byteBuffer = new byte[floatArray.length * format.getFrameSize()];
		converter.toByteArray(floatArray, byteBuffer);
		final ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		final long length = floatArray.length;
		final AudioInputStream stream = new AudioInputStream(bais, format, length);
		TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
		return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
	}
} 