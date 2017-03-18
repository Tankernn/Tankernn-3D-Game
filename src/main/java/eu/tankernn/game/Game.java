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
import eu.tankernn.gameEngine.particles.ParticleMaster;
import eu.tankernn.gameEngine.postProcessing.PostProcessor;
import eu.tankernn.gameEngine.renderEngine.MasterRenderer;
import eu.tankernn.gameEngine.renderEngine.water.WaterMaster;
import eu.tankernn.gameEngine.renderEngine.water.WaterTile;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.util.MousePicker;

public class Game extends TankernnGame3D {
	public Game() {
		super(Settings.GAME_NAME, TEXTURE_FILES, NIGHT_TEXTURE_FILES, new Light(new Vector3f(1, 1000, 1000), new Vector3f(1f, 1f, 1f)));
		
		try {
			setupTerrain();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		player = new Player(loader.getModel(3), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1, loader.getBoundingBox(0), terrainPack);
		
		entities.add(player);
		camera = new PlayerCamera(player, terrainPack);
		
		renderer = new MasterRenderer(loader, camera, sky);
		try {
			waterMaster = new WaterMaster(loader, loader.loadTexture(DUDV_MAP), loader.loadTexture(NORMAL_MAP), camera);
			setupWater();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		particleMaster = new ParticleMaster(loader, camera.getProjectionMatrix());
		
		entities.add(new Entity3D(loader.getModel(2), new Vector3f(10, 10, 10), new Vector3f(0, 0, 0), 1, loader.getBoundingBox(2)));
		
		entities.add(new Entity3D(loader.getModel(3), new Vector3f(10, 10, 10), new Vector3f(0, 0, 0), 1, loader.getBoundingBox(2)));
		
		postProcessor = new PostProcessor(loader);
		picker = new MousePicker(camera, camera.getProjectionMatrix(), entities, guiMaster.getGuis());
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
		WaterTile water = new WaterTile(50, 50, 0, 50);
		waterMaster.addWaterTile(water);
	}
	
	public void update() {
		super.update();
		if (picker.getCurrentTerrainPoint() != null) {
			entities.get(1).setPosition(picker.getCurrentTerrainPoint());
		}
	}
	
	public static void main(String[] args) {
		GameLauncher.init(Settings.GAME_NAME, 1600, 900);
		GameLauncher.launch(new Game());
	}
}
