package eu.tankernn.game;

import java.io.IOException;

import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.audio.AudioMaster;
import eu.tankernn.gameEngine.audio.Source;

public class AudioTester {
	public static void main(String[] args) throws IOException, InterruptedException {
		AudioMaster master = new AudioMaster();
		master.setListenerData(0, 0, 0);
		AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
		
		int buffer = master.loadSound("sound/bounce.wav");
		Source source = new Source();
		source.setLooping(true);
		source.play(buffer);
		
		float xPos = 0;
		source.setPosition(new Vector3f(xPos, 0, 0));
		
		char c = ' ';
		while (c != 'q') {
			//c = (char) System.in.read();
			
			xPos -= 0.03f;
			source.setPosition(new Vector3f(xPos, 0, 0));
			System.out.println(xPos);
			Thread.sleep(10);
			
		}
		
		source.delete();
		master.finalize();
		
	}
}
