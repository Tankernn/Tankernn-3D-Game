package eu.tankernn.game;

public class Settings {
	public static final boolean ONLINE = true;
	public static final boolean DEBUG = false;
	public static final String GAME_NAME = "Tankernn game engine tester";

	// Skybox settings
	public static final String[] TEXTURE_FILES = { "alps_ft", "alps_bk", "alps_up", "alps_dn", "alps_rt", "alps_lf" };
	public static final String[] NIGHT_TEXTURE_FILES = { "midnight_ft", "midnight_bk", "midnight_up", "midnight_dn",
			"midnight_rt", "midnight_lf" };

	// Water settings
	public static final String DUDV_MAP = "textures/waterDUDV.png";
	public static final String NORMAL_MAP = "textures/waterNormalMap.png";
}
