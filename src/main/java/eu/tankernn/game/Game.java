package eu.tankernn.game;

import static eu.tankernn.game.Settings.DUDV_MAP;
import static eu.tankernn.game.Settings.NIGHT_TEXTURE_FILES;
import static eu.tankernn.game.Settings.NORMAL_MAP;
import static eu.tankernn.game.Settings.TEXTURE_FILES;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame3D;
import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.entities.Light;
import eu.tankernn.gameEngine.entities.Player;
import eu.tankernn.gameEngine.entities.PlayerCamera;
import eu.tankernn.gameEngine.loader.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.Scene;
import eu.tankernn.gameEngine.renderEngine.gui.GuiTexture;
import eu.tankernn.gameEngine.renderEngine.skybox.Skybox;
import eu.tankernn.gameEngine.renderEngine.water.WaterTile;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.util.DistanceSorter;
import eu.tankernn.gameEngine.util.InternalFile;
import eu.tankernn.gameEngine.util.MousePicker;

public class Game extends TankernnGame3D {
	MousePicker picker;

	List<Entity3D> entities;
	List<Light> lights;
	List<GuiTexture> guis;
	Light sun;

	TerrainPack terrainPack;

	Player player;

	public Game() {
		super(Settings.GAME_NAME, new Skybox(Texture.newCubeMap(InternalFile.fromFilenames("skybox", TEXTURE_FILES, "png"), 200), Texture.newCubeMap(InternalFile.fromFilenames("skybox", NIGHT_TEXTURE_FILES, "png"), 200), 400), DUDV_MAP, NORMAL_MAP);
		entities = new ArrayList<Entity3D>();

		lights = new ArrayList<Light>();
		sun = new Light(new Vector3f(1000, 1000, 0), new Vector3f(1f, 1f, 1f));
		lights.add(sun);

		try {
			setupTerrain();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setupWater();
		setupGuis();

		player = new Player(0, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1,
				loader.getModel(0).getRawModel().getBoundingBox(), terrainPack);
		camera = new PlayerCamera(player, terrainPack);
		entities.add(player);
		entities.add(new Entity3D(2, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1,
				loader.getModel(2).getRawModel().getBoundingBox()));
		lights.add(new Light(new Vector3f(0, 1000, 0), new Vector3f(1f, 1f, 1f)));
		picker = new MousePicker(camera, camera.getProjectionMatrix(), terrainPack, entities, guis);
	}

	private void setupTerrain() throws FileNotFoundException {
		Texture backgroundTexture = loader.loadTexture("grassy.png");
		Texture rTexture = loader.loadTexture("dirt.png");
		Texture gTexture = loader.loadTexture("pinkFlowers.png");
		Texture bTexture = loader.loadTexture("path.png");

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		Texture blendMap = loader.loadTexture("blendMap.png");

		terrainPack = new TerrainPack(loader, texturePack, blendMap, 1337);
	}

	private void setupWater() {
		WaterTile water = new WaterTile(50, 50, 0, 60);
		waterMaster.addWaterTile(water);
	}

	private void setupGuis() {
		guis = new ArrayList<GuiTexture>();
	}

	public void update() {
		player.move();
		camera.update();
		picker.update();
		terrainPack.update(player);
		if (picker.getCurrentTerrainPoint() != null) {
			entities.get(1).setPosition(picker.getCurrentTerrainPoint());
		}

	}

	public void render() {
		// Sort list of lights
		DistanceSorter.sort(lights, camera);

		renderer.renderShadowMap(entities, sun);

		Scene scene = new Scene(entities, terrainPack, lights, camera, sky);
		waterMaster.renderBuffers(renderer, scene);
		renderer.renderScene(scene, new Vector4f(0, 1, 0, Float.MAX_VALUE));
		waterMaster.renderWater(camera, lights);
		DisplayManager.updateDisplay();
	}
	
	public static void main(String[] args) {
		GameLauncher.init(Settings.GAME_NAME);
		GameLauncher.launch(new Game());
	}
}
