package eu.tankernn.game.test;

import org.junit.Test;

import eu.tankernn.game.Game;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;

public class GameTest {
	@Test
	public void gameShouldInit() {
		DisplayManager.createDisplay("Test", 200, 200);
		Game tester = new Game();
		
		tester.update();
		tester.render();
		tester.cleanUp();
	}
}
