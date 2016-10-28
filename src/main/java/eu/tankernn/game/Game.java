package eu.tankernn.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import eu.tankernn.gameEngine.TankernnGame;
import eu.tankernn.gameEngine.entities.Entity;
import eu.tankernn.gameEngine.entities.Light;
import eu.tankernn.gameEngine.entities.Player;
import eu.tankernn.gameEngine.entities.PlayerCamera;
import eu.tankernn.gameEngine.models.TexturedModel;
import eu.tankernn.gameEngine.objLoader.OBJFileLoader;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.Scene;
import eu.tankernn.gameEngine.terrains.Terrain;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.textures.ModelTexture;
import eu.tankernn.gameEngine.textures.TerrainTexture;
import eu.tankernn.gameEngine.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.util.DistanceSorter;

public class Game extends TankernnGame {
	List<Entity> entities;
	List<Entity> normalEntities;
	List<Light> lights;
	Light sun;
	
	TerrainPack terrainPack;
	
	Player player;
	
	public Game() {
		super(Settings.TEXTURE_FILES, Settings.NIGHT_TEXTURE_FILES);
		entities = new ArrayList<Entity>();
		normalEntities = new ArrayList<Entity>();
		
		lights = new ArrayList<Light>();
		sun = new Light(new Vector3f(1000, 1000, 0), new Vector3f(1f, 1f, 1f));
		lights.add(sun);
		
		setupTerrain();
		
		player = new Player(new TexturedModel(loader.loadToVAO(OBJFileLoader.loadOBJ("character")), new ModelTexture(loader.loadTexture("white"))), new Vector3f(0, 0, 0), 0, 0, 0, 1, terrainPack);
		camera = new PlayerCamera(player, terrainPack);
		entities.add(player);
		lights.add(new Light(new Vector3f(0, 1000, 0), new Vector3f(1f, 1f, 1f)));
	}
	
	private void setupTerrain() {
		terrainPack = new TerrainPack();
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		terrainPack.addTerrain(new Terrain(0, 0, loader, texturePack, blendMap, 1337));
	}
	
	public void update() {
		player.move(terrainPack);
		camera.update();
	}
	
	public void render() {
		//Sort list of lights
		DistanceSorter.sort(lights, camera);
		
		renderer.renderShadowMap(entities, sun);
		
		Scene scene = new Scene(entities, normalEntities, terrainPack, lights, camera);
		renderer.renderScene(scene, new Vector4f(0, 1, 0, Float.MAX_VALUE));
		DisplayManager.updateDisplay();
	}
}
