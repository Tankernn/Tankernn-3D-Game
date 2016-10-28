package eu.tankernn.game;

import java.io.IOException;

import org.lwjgl.openal.AL10;

import eu.tankernn.gameEngine.audio.AudioMaster;
import eu.tankernn.gameEngine.audio.Source;

public class AudioTester {
	public static void main(String[] args) throws IOException, InterruptedException {
		AudioMaster.init();
		AudioMaster.setListenerData(0, 0, 0);
		AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
		
		int buffer = AudioMaster.loadSound("sound/bounce.wav");
		Source source = new Source();
		source.setLooping(true);
		source.play(buffer);
		
		float xPos = 0;
		source.setPosition(xPos, 0, 0);
		
		char c = ' ';
		while (c != 'q') {
			//c = (char) System.in.read();
			
			xPos -= 0.03f;
			source.setPosition(xPos, 0, 0);
			System.out.println(xPos);
			Thread.sleep(10);
			
		}
		
		source.delete();
		AudioMaster.cleanUp();
		
	}
}
