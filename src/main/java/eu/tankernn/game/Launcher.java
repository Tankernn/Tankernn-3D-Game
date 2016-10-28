package eu.tankernn.game;

import org.lwjgl.opengl.Display;

import eu.tankernn.gameEngine.TankernnGame;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.util.NativesExporter;

import static eu.tankernn.game.Settings.*;

public class Launcher {
	
	public static TankernnGame instance;
	
	public static void main(String[] args) {
		init();
		
		while (!Display.isCloseRequested()) {
			instance.update();
			instance.render();
		}
		
		instance.cleanUp();
	}
	
	private static void init() {
		NativesExporter.exportNatives();
		DisplayManager.createDisplay(GAME_NAME);
		instance = new Game();
	}
	
}
