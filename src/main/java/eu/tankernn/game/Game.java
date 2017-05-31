package eu.tankernn.game;

import static eu.tankernn.game.Settings.DUDV_MAP;
import static eu.tankernn.game.Settings.GAME_NAME;
import static eu.tankernn.game.Settings.NIGHT_TEXTURE_FILES;
import static eu.tankernn.game.Settings.NORMAL_MAP;
import static eu.tankernn.game.Settings.ONLINE;
import static eu.tankernn.game.Settings.TEXTURE_FILES;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.game.networking.GameClientHandler;
import eu.tankernn.gameEngine.GameLauncher;
import eu.tankernn.gameEngine.TankernnGame3D;
import eu.tankernn.gameEngine.World;
import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.entities.EntityState;
import eu.tankernn.gameEngine.entities.PlayerBehavior;
import eu.tankernn.gameEngine.entities.PlayerCamera;
import eu.tankernn.gameEngine.entities.ai.DieOnCollisionBehavior;
import eu.tankernn.gameEngine.entities.ai.FollowBehavior;
import eu.tankernn.gameEngine.entities.projectiles.ProjectileState;
import eu.tankernn.gameEngine.loader.font.Font;
import eu.tankernn.gameEngine.loader.font.FontFamily;
import eu.tankernn.gameEngine.loader.font.GUIText;
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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Game extends TankernnGame3D {
	private float cooldown;

	private GUIText fpsText, text;
	private Font font;

	private FlareManager flareManager;
	private Sun sun;

	private Channel channel;
	private EventLoopGroup workerGroup;

	private float timeSinceStart;

	public Game() {
		super(GAME_NAME, TEXTURE_FILES, NIGHT_TEXTURE_FILES);

		TerrainPack terrain;
		try {
			terrain = setupTerrain();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			ParticleSystem system = new ParticleSystem(
					new ParticleTexture(loader.loadTexture(new InternalFile("particles/cosmic.png")), 4, true), 50, 1,
					0, 1);
			loader.registerParticleSystem(1, system);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		camera = new PlayerCamera(player, terrain);
		particleMaster = new ParticleMaster(loader, camera.getProjectionMatrix());
		world = new World(loader, particleMaster, terrain);

		if (!ONLINE) {
			EntityState playerState = new EntityState(0, -1, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
					new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
			playerState.addBehavior(new PlayerBehavior());
			player = world.updateEntityState(playerState);
		}

		renderer = new MasterRenderer(loader, camera, sky);
		try {
			waterMaster = new WaterMaster(loader, loader.loadTexture(new InternalFile(DUDV_MAP)),
					loader.loadTexture(new InternalFile(NORMAL_MAP)), camera);
			WaterTile water = new WaterTile(50, 50, 0, 50);
			waterMaster.addWaterTile(water);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setupFlares();

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
			world.getLights().add(sun);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (ONLINE)
			connectOnline();
	}

	private void connectOnline() {
		workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("objectDecoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
					ch.pipeline().addLast("objectEncoder", new ObjectEncoder());
					ch.pipeline().addLast("timeouthandler", new ReadTimeoutHandler(30));
					ch.pipeline().addLast(new GameClientHandler(Game.this));
				}
			});

			// Start the client.
			channel = bootstrap.connect("localhost", 25566).sync().channel();
		} catch (InterruptedException e) {
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

	private TerrainPack setupTerrain() throws FileNotFoundException {
		Texture backgroundTexture = loader.loadTexture(new InternalFile("textures/grassy.png"));
		Texture rTexture = loader.loadTexture(new InternalFile("textures/dirt.png"));
		Texture gTexture = loader.loadTexture(new InternalFile("textures/pinkFlowers.png"));
		Texture bTexture = loader.loadTexture(new InternalFile("textures/path.png"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		Texture blendMap = loader.loadTexture(new InternalFile("textures/blendMap.png"));

		return new TerrainPack(loader, texturePack, blendMap, 1235);
	}

	public void update() {
		super.update();
		timeSinceStart += DisplayManager.getFrameTimeSeconds();
		if (waterMaster.isPointUnderWater(camera.getPosition()) && postProcessor.blurFactor < 2)
			postProcessor = new PostProcessor(loader, true);
		else if (!waterMaster.isPointUnderWater(camera.getPosition()) && postProcessor.blurFactor > 0)
			postProcessor = new PostProcessor(loader, false);
		// if (picker.getCurrentTerrainPoint() != null) {
		// entities.get(1).setPosition(picker.getCurrentTerrainPoint());
		// }

		// Update debug info
		if (true) {
			Terrain currentTerrain = world.getTerrainPack().getTerrainByWorldPos(player.getPosition().x,
					player.getPosition().z);
			if (currentTerrain != null) {
				Vector3f pos = player.getPosition();
				String textString = "X: " + Math.floor(pos.x) + " Y: " + Math.floor(pos.y) + " Z: " + Math.floor(pos.z)
						+ " Current terrain: " + currentTerrain.getX() / Settings.TERRAIN_SIZE + ":"
						+ currentTerrain.getZ() / Settings.TERRAIN_SIZE;
				text.setText(textString);
				fpsText.setText(String.format("FPS: %.2f", 1f / DisplayManager.getFrameTimeSeconds()));
			}
		}

		int daylength = 6000;

		float progress = timeSinceStart * 1000;
		progress %= daylength;
		progress /= daylength;
		progress *= 2;
		progress -= 1;
		sun.setDirection(progress, 0f, 0f);

		for (Entity3D e : world.getEntities().values())
			if (e.equals(picker.getCurrentEntity()))
				e.setScale(new Vector3f(2, 2, 2));
			else
				e.setScale(new Vector3f(1, 1, 1));
		if (Keyboard.isKeyDown(Keyboard.KEY_E) && cooldown <= 0) {
			ProjectileState state = new ProjectileState(-1, 1, player.getPosition(), new Vector3f(0, 0, 0),
					new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), 40);
			// int index =
			// world.getEntities().values().stream().map(Entity3D::getState).filter(e
			// -> e.getId() != player.getId()).findFirst().get().getId();
			state.addBehavior(new FollowBehavior(new Vector3f(0, 0, 0), 10));
			state.addBehavior(new DieOnCollisionBehavior());
			if (!ONLINE)
				world.updateEntityState(state);
			else
				channel.writeAndFlush(state).syncUninterruptibly();

			Vector3f pos = new Vector3f(player.getPosition());
			pos.y += 20;
			particleMaster.addTextParticle("10", font, pos);
			cooldown = 1;
		}

		if (cooldown > 0)
			cooldown -= DisplayManager.getFrameTimeSeconds();

		if (channel != null) { // Send player pos to server
			channel.writeAndFlush(player.getState());
		}

	}

	@Override
	public void preRender() {
		renderer.renderShadowMap(world.getEntities().values(), sun);
	}

	@Override
	public void render() {
		super.render();
		flareManager.render(camera, sun.getPosition());
	}

	@Override
	public void cleanUp() {
		if (channel != null && channel.isOpen())
			channel.close();
		if (workerGroup != null)
			try {
				workerGroup.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		super.cleanUp();
	}

	public void setPlayer(Entity3D spawnEntity) {
		this.player = spawnEntity;
		if (this.camera instanceof PlayerCamera)
			((PlayerCamera) this.camera).setPlayer(spawnEntity);
	}

	public static void main(String[] args) {
		GameLauncher.init(Settings.GAME_NAME, 1600, 900);
		GameLauncher.launch(new Game());
	}
}
