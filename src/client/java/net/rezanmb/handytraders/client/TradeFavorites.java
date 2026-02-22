package net.rezanmb.handytraders.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.rezanmb.handytraders.HandyTraders;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Per-villager trade favorites storage.
 * Persisted as JSON at config/handytraders-favorites.json.
 *
 * Structure: { "<villager-uuid>": { "favorites": ["<trade-hash>", ...] } }
 */
@Environment(EnvType.CLIENT)
public final class TradeFavorites {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FAVORITES_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handytraders-favorites.json");
	private static final Type DATA_TYPE = new TypeToken<Map<String, VillagerData>>() {}.getType();

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
		if (Files.exists(FAVORITES_PATH)) {
			try (Reader reader = Files.newBufferedReader(FAVORITES_PATH)) {
				Map<String, VillagerData> loaded = GSON.fromJson(reader, DATA_TYPE);
				if (loaded != null) {
					data = loaded;
					int totalFavs = loaded.values().stream()
							.mapToInt(vd -> vd.favorites.size()).sum();
					HandyTraders.LOGGER.info("Loaded {} favorites for {} villagers from {}",
							totalFavs, loaded.size(), FAVORITES_PATH);
				}
			} catch (Exception e) {
				HandyTraders.LOGGER.warn("Failed to load trade favorites", e);
			}
		} else {
			HandyTraders.LOGGER.info("No favorites file at {}", FAVORITES_PATH);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(FAVORITES_PATH.getParent());
			Files.writeString(FAVORITES_PATH, GSON.toJson(data, DATA_TYPE));
		} catch (IOException e) {
			HandyTraders.LOGGER.warn("Failed to save trade favorites", e);
		}
	}

	public static class VillagerData {
		public Set<String> favorites = new HashSet<>();
	}
}
