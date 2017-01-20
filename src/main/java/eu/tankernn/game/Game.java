package eu.tankernn.game;

import static eu.tankernn.game.Settings.DUDV_MAP;
import static eu.tankernn.game.Settings.NIGHT_TEXTURE_FILES;
import static eu.tankernn.game.Settings.NORMAL_MAP;
import static eu.tankernn.game.Settings.TEXTURE_FILES;

import java.io.FileNotFoundException;

import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame3D;
import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.entities.Light;
import eu.tankernn.gameEngine.entities.Player;
import eu.tankernn.gameEngine.entities.PlayerCamera;
import eu.tankernn.gameEngine.loader.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.skybox.Skybox;
import eu.tankernn.gameEngine.renderEngine.water.WaterTile;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.util.InternalFile;

public class Game extends TankernnGame3D {
	public Game() {
		super(Settings.GAME_NAME,
				new Skybox(Texture.newCubeMap(InternalFile.fromFilenames("skybox", TEXTURE_FILES, "png"), 200),
						Texture.newCubeMap(InternalFile.fromFilenames("skybox", NIGHT_TEXTURE_FILES, "png"), 200), 400),
				DUDV_MAP, NORMAL_MAP, new Light(new Vector3f(1000, 1000, 0), new Vector3f(1f, 1f, 1f)));

		try {
			setupTerrain();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setupWater();

		player = new Player(0, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1,
				loader.getModel(0).getRawModel().getBoundingBox(), terrainPack);
		camera = new PlayerCamera(player, terrainPack);

		entities.add(new Entity3D(2, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1,
				loader.getModel(2).getRawModel().getBoundingBox()));
		lights.add(new Light(new Vector3f(0, 1000, 0), new Vector3f(1f, 1f, 1f)));
	}

	private void setupTerrain() throws FileNotFoundException {
		Texture backgroundTexture = loader.loadTexture("grassy.png");
		Texture rTexture = loader.loadTexture("dirt.png");
		Texture gTexture = loader.loadTexture("pinkFlowers.png");
		Texture bTexture = loader.loadTexture("path.png");

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		Texture blendMap = loader.loadTexture("blendMap.png");

		terrainPack = new TerrainPack(loader, texturePack, blendMap, 1235);
	}

	private void setupWater() {
		WaterTile water = new WaterTile(50, 50, 0, 60);
		waterMaster.addWaterTile(water);
	}

	public void update() {
		super.update();
		if (picker.getCurrentTerrainPoint() != null) {
			entities.get(1).setPosition(picker.getCurrentTerrainPoint());
		}
	}

	public static void main(String[] args) {
		GameLauncher.init(Settings.GAME_NAME);
		GameLauncher.launch(new Game());
	}
}
