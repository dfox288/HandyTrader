package dev.handy.mods.handytrader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.handy.mods.handytrader.HandyTrader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for Handy Trader.
 * Persisted as JSON in config/handytrader.json.
 */
public class HandyTraderConfig {

	private static HandyTraderConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytrader.json");
	// Legacy path from before the v2.1 mod-id rename (handytraders → handytrader).
	// Read once on first load so user settings carry over; safe to remove after a few releases.
	private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytraders.json");

	public boolean enableFavorites = true;

	public static HandyTraderConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH) && Files.exists(LEGACY_CONFIG_PATH)) {
			try {
				Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
				HandyTrader.LOGGER.info("Migrated config from {} to {}",
						LEGACY_CONFIG_PATH.getFileName(), CONFIG_PATH.getFileName());
			} catch (IOException e) {
				HandyTrader.LOGGER.warn("Failed to migrate legacy config from {}",
						LEGACY_CONFIG_PATH.getFileName(), e);
			}
		}

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyTraderConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyTraderConfig();
				}
			} catch (IOException | JsonParseException e) {
				HandyTrader.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyTraderConfig();
			}
		} else {
			INSTANCE = new HandyTraderConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyTrader.LOGGER.warn("Failed to save config", e);
		}
	}
}
