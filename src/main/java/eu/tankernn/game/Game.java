package eu.tankernn.game;

import static eu.tankernn.game.Settings.DUDV_MAP;
import static eu.tankernn.game.Settings.GAME_NAME;
import static eu.tankernn.game.Settings.NIGHT_TEXTURE_FILES;
import static eu.tankernn.game.Settings.NORMAL_MAP;
import static eu.tankernn.game.Settings.TEXTURE_FILES;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame3D;
import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.entities.Player;
import eu.tankernn.gameEngine.entities.PlayerCamera;
import eu.tankernn.gameEngine.entities.npc.NPC;
import eu.tankernn.gameEngine.entities.npc.RoamingArea;
import eu.tankernn.gameEngine.entities.npc.RoamingBehavior;
import eu.tankernn.gameEngine.entities.projectiles.Projectile;
import eu.tankernn.gameEngine.entities.projectiles.TargetedProjectile;
import eu.tankernn.gameEngine.loader.font.Font;
import eu.tankernn.gameEngine.loader.font.FontFamily;
import eu.tankernn.gameEngine.loader.font.GUIText;
import eu.tankernn.gameEngine.loader.models.AABB;
import eu.tankernn.gameEngine.loader.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.particles.ParticleMaster;
import eu.tankernn.gameEngine.particles.ParticleSystem;
import eu.tankernn.gameEngine.particles.ParticleTexture;
import eu.tankernn.gameEngine.particles.Sun;
import eu.tankernn.gameEngine.postProcessing.PostProcessor;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.MasterRenderer;
import eu.tankernn.gameEngine.renderEngine.gui.GuiTexture;
import eu.tankernn.gameEngine.renderEngine.gui.floating.FloatingTextureRenderer;
import eu.tankernn.gameEngine.renderEngine.lensFlare.FlareManager;
import eu.tankernn.gameEngine.renderEngine.water.WaterMaster;
import eu.tankernn.gameEngine.renderEngine.water.WaterTile;
import eu.tankernn.gameEngine.settings.Settings;
import eu.tankernn.gameEngine.terrains.Terrain;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.util.InternalFile;
import eu.tankernn.gameEngine.util.MousePicker;

public class Game extends TankernnGame3D {
	private float cooldown;

	private GUIText fpsText, text;
	private Font font;

	private FlareManager flareManager;
	private Sun sun;

	public Game() {
		super(GAME_NAME, TEXTURE_FILES, NIGHT_TEXTURE_FILES);

		try {
			setupTerrain();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		player = new Player(loader.getModel(0), new Vector3f(0, 0, 0),
				loader.getBoundingBox(loader.getModel(0).getModel().id), terrainPack);

		entities.add(player);
		camera = new PlayerCamera(player, terrainPack);

		renderer = new MasterRenderer(loader, camera, sky);
		try {
			waterMaster = new WaterMaster(loader, loader.loadTexture(new InternalFile(DUDV_MAP)),
					loader.loadTexture(new InternalFile(NORMAL_MAP)), camera);
			WaterTile water = new WaterTile(50, 50, 0, 50);
			waterMaster.addWaterTile(water);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		particleMaster = new ParticleMaster(loader, camera.getProjectionMatrix());
		setupFlares();

		entities.add(new Entity3D(loader.getModel(2), new Vector3f(10, 10, 10),
				loader.getBoundingBox(loader.getModel(2).getModel().id), terrainPack));

		entities.add(new Entity3D(loader.getModel(3), new Vector3f(10, 10, 10),
				loader.getBoundingBox(loader.getModel(3).getModel().id), terrainPack));

		RoamingArea roam = new RoamingArea(new Vector2f(0, 0), new Vector2f(100, 100));

		for (int i = 0; i < 10; i++)
			entities.add(new NPC(loader.getModel(1), new Vector3f(0, 0, 0), 1,
					loader.getBoundingBox(loader.getModel(1).getModel().id), terrainPack,
					new RoamingBehavior(roam, 10)));

		postProcessor = new PostProcessor(loader);
		picker = new MousePicker(camera);

		try {
			font = new Font(new FontFamily(loader.loadTextureAtlas(new InternalFile("arial.png")),
					new InternalFile("arial.fnt")), 2, new Vector3f(1, 0, 1), new Vector3f(0.2f, 0.2f, 0.2f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		text = new GUIText("Sample text", font, new Vector2f(0.5f, 0.0f), 0.5f, false);
		fpsText = new GUIText("FPS: ", font, new Vector2f(0.0f, 0.0f), 0.5f, false);
		textMaster.loadText(fpsText);
		textMaster.loadText(text);

		floatingRenderer = new FloatingTextureRenderer(loader, camera.getProjectionMatrix());

		try {
			this.sun = new Sun(new ParticleTexture(loader.loadTexture(new InternalFile("lensFlare/sun.png")), 1, true),
					30, new Vector3f(1f, 1f, 1f));
			sun.setDirection(-0.8f, -0.5f, 0f);
			particleMaster.addParticle(sun);
			lights.add(sun);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setupFlares() {
		String[] filenames = IntStream.rangeClosed(1, 9).mapToObj(i -> "tex" + i).toArray(String[]::new);
		InternalFile[] files = InternalFile.fromFilenames("lensFlare", filenames, "png");
		Texture[] textures = Arrays.stream(files).map(f -> loader.loadTexture(f)).toArray(Texture[]::new);

		flareManager = new FlareManager(guiMaster.getRenderer(), 0.16f, new GuiTexture(textures[5], 1f),
				new GuiTexture(textures[3], 0.46f), new GuiTexture(textures[1], 0.2f),
				new GuiTexture(textures[6], 0.1f), new GuiTexture(textures[0], 0.04f),
				new GuiTexture(textures[2], 0.12f), new GuiTexture(textures[8], 0.24f),
				new GuiTexture(textures[4], 0.14f), new GuiTexture(textures[0], 0.024f),
				new GuiTexture(textures[6], 0.4f), new GuiTexture(textures[8], 0.2f),
				new GuiTexture(textures[2], 0.14f), new GuiTexture(textures[4], 0.6f),
				new GuiTexture(textures[3], 0.8f), new GuiTexture(textures[7], 1.2f));

	}

	private void setupTerrain() throws FileNotFoundException {
		Texture backgroundTexture = loader.loadTexture(new InternalFile("textures/grassy.png"));
		Texture rTexture = loader.loadTexture(new InternalFile("textures/dirt.png"));
		Texture gTexture = loader.loadTexture(new InternalFile("textures/pinkFlowers.png"));
		Texture bTexture = loader.loadTexture(new InternalFile("textures/path.png"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		Texture blendMap = loader.loadTexture(new InternalFile("textures/blendMap.png"));

		terrainPack = new TerrainPack(loader, texturePack, blendMap, 1235);
	}

	public void update() {
		super.update();
		if (waterMaster.isPointUnderWater(camera.getPosition()) && postProcessor.blurFactor < 2)
			postProcessor = new PostProcessor(loader, true);
		else if (!waterMaster.isPointUnderWater(camera.getPosition()) && postProcessor.blurFactor > 0)
			postProcessor = new PostProcessor(loader, false);
		if (picker.getCurrentTerrainPoint() != null) {
			entities.get(1).setPosition(picker.getCurrentTerrainPoint());
		}

		// Update debug info
		if (true) {
			Terrain currentTerrain = terrainPack.getTerrainByWorldPos(player.getPosition().x, player.getPosition().z);
			if (currentTerrain != null) {
				Vector3f pos = player.getPosition();
				String textString = "X: " + Math.floor(pos.x) + " Y: " + Math.floor(pos.y) + " Z: " + Math.floor(pos.z)
						+ " Current terrain: " + currentTerrain.getX() / Settings.TERRAIN_SIZE + ":"
						+ currentTerrain.getZ() / Settings.TERRAIN_SIZE;
				text.setText(textString);
				fpsText.setText(String.format("FPS: %.2f", 1f / DisplayManager.getFrameTimeSeconds()));
			}
		}

		for (Entity3D e : entities)
			if (e.equals(picker.getCurrentEntity()))
				e.setScale(new Vector3f(2, 2, 2));
			else
				e.setScale(new Vector3f(1, 1, 1));
		if (Keyboard.isKeyDown(Keyboard.KEY_E) && cooldown <= 0) {
			try {
				ParticleSystem system = new ParticleSystem(
						new ParticleTexture(loader.loadTexture(new InternalFile("particles/cosmic.png")), 4, true), 50,
						1, 0, 1);
				particleMaster.addSystem(system);

				Projectile p = new TargetedProjectile(terrainPack, null, new Vector3f(player.getPosition()),
						entities.get(1), 50, new AABB(new Vector3f(0, 0, 0), new Vector3f(0.1f, 0.1f, 0.1f)), system);
				projectiles.add(p);
				Vector3f pos = new Vector3f(player.getPosition());
				pos.y += 20;
				particleMaster.addTextParticle("10", font, pos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			cooldown = 1;
		}

		if (cooldown > 0)
			cooldown -= DisplayManager.getFrameTimeSeconds();
	}

	@Override
	public void preRender() {
		renderer.renderShadowMap(entities, sun);
	}

	@Override
	public void render() {
		super.render();
		flareManager.render(camera, sun.getPosition());
	}

	public static void main(String[] args) {
		GameLauncher.init(Settings.GAME_NAME, 1600, 900);
		GameLauncher.launch(new Game());
	}
}
