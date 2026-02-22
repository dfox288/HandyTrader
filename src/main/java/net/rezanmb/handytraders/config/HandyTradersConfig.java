package net.rezanmb.handytraders.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.rezanmb.handytraders.HandyTraders;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for Handy Traders.
 * Persisted as JSON in config/handytraders.json.
 */
public class HandyTradersConfig {

	private static HandyTradersConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytraders.json");

	public boolean enableFavorites = true;
	public boolean sortFavoritesToTop = false;
	public boolean showStarButton = true;
	public boolean playSounds = true;

	public static HandyTradersConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyTradersConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyTradersConfig();
				}
			} catch (Exception e) {
				HandyTraders.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyTradersConfig();
			}
		} else {
			INSTANCE = new HandyTradersConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyTraders.LOGGER.warn("Failed to save config", e);
		}
	}
}
