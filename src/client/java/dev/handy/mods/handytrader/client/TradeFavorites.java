package dev.handy.mods.handytrader.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import dev.handy.mods.handytrader.HandyTrader;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Per-villager trade favorites storage.
 * Persisted as JSON at config/handytrader-favorites.json.
 *
 * Structure: { "<villager-uuid>": { "favorites": ["<trade-hash>", ...] } }
 */
@Environment(EnvType.CLIENT)
public final class TradeFavorites {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FAVORITES_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytrader-favorites.json");
	// Legacy path from before the v2.1 mod-id rename (handytraders → handytrader).
	// Read once on first load so user favorites carry over; safe to remove after a few releases.
	private static final Path LEGACY_FAVORITES_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytraders-favorites.json");
	private static final Type DATA_TYPE = new TypeToken<Map<String, VillagerData>>() {}.getType();
	private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "HandyTrader-favorites-save");
		t.setDaemon(true);
		return t;
	});

	private static Map<String, VillagerData> data = new HashMap<>();

	private TradeFavorites() {}

	public static boolean isFavorite(UUID villagerUUID, String tradeHash) {
		VillagerData vd = data.get(villagerUUID.toString());
		return vd != null && vd.favorites.contains(tradeHash);
	}

	public static void toggleFavorite(UUID villagerUUID, String tradeHash) {
		String key = villagerUUID.toString();
		VillagerData vd = data.computeIfAbsent(key, k -> new VillagerData());
		if (!vd.favorites.remove(tradeHash)) {
			vd.favorites.add(tradeHash);
		}
		// Clean up empty entries
		if (vd.favorites.isEmpty()) {
			data.remove(key);
		}
	}

	public static void load() {
		if (!Files.exists(FAVORITES_PATH) && Files.exists(LEGACY_FAVORITES_PATH)) {
			try {
				Files.copy(LEGACY_FAVORITES_PATH, FAVORITES_PATH);
				HandyTrader.LOGGER.info("Migrated favorites from {} to {}",
						LEGACY_FAVORITES_PATH.getFileName(), FAVORITES_PATH.getFileName());
			} catch (IOException e) {
				HandyTrader.LOGGER.warn("Failed to migrate legacy favorites from {}",
						LEGACY_FAVORITES_PATH.getFileName(), e);
			}
		}

		if (Files.exists(FAVORITES_PATH)) {
			try (Reader reader = Files.newBufferedReader(FAVORITES_PATH)) {
				Map<String, VillagerData> loaded = GSON.fromJson(reader, DATA_TYPE);
				if (loaded != null) {
					data = loaded;
					int totalFavs = loaded.values().stream()
							.mapToInt(vd -> vd.favorites.size()).sum();
					HandyTrader.LOGGER.info("Loaded {} favorites for {} villagers from {}",
							totalFavs, loaded.size(), FAVORITES_PATH);
				}
			} catch (IOException | JsonParseException e) {
				HandyTrader.LOGGER.warn("Failed to load trade favorites", e);
			}
		} else {
			HandyTrader.LOGGER.info("No favorites file at {}", FAVORITES_PATH);
		}
	}

	public static void save() {
		// Snapshot serialize on the caller thread so the daemon writer never races
		// the render-thread mutator. Disk I/O is what we want off the render thread,
		// not the in-memory toJson which is microseconds.
		String json = GSON.toJson(data, DATA_TYPE);
		SAVE_EXECUTOR.execute(() -> {
			try {
				Files.createDirectories(FAVORITES_PATH.getParent());
				Files.writeString(FAVORITES_PATH, json);
			} catch (IOException e) {
				HandyTrader.LOGGER.warn("Failed to save trade favorites", e);
			}
		});
	}

	public static class VillagerData {
		public Set<String> favorites = new HashSet<>();
	}
}
