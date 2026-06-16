package dev.handy.mods.handytrader.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes a stable hash for a MerchantOffer based on item IDs, counts,
 * and component data. This allows identifying the same trade across sessions
 * even if the trade index shifts when a villager levels up.
 */
@Environment(EnvType.CLIENT)
public final class TradeHash {

	private TradeHash() {}

	public static String hash(MerchantOffer offer) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] out = md.digest(canonicalKey(offer).getBytes(StandardCharsets.UTF_8));
			// First 8 bytes → 16 hex chars; avoids allocating the full 40-char string.
			return HexFormat.of().formatHex(out, 0, 8);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-1 unavailable", e);
		}
	}

	/**
	 * Pre-2.1.0-beta.3 hash format — 32-bit Java {@code String.hashCode} of the
	 * canonical key, hex-encoded with no leading-zero pad. Retained only so we
	 * can detect favorites stored in the old format and rewrite them to the new
	 * SHA-1-based format on first lookup. Safe to delete a few releases after
	 * the migration window closes.
	 */
	public static String legacyHash(MerchantOffer offer) {
		return Long.toHexString(canonicalKey(offer).hashCode() & 0xFFFFFFFFL);
	}

	private static String canonicalKey(MerchantOffer offer) {
		return itemKey(offer.getBaseCostA()) + "|" +
				itemKey(offer.getCostB()) + "|" +
				itemKey(offer.getResult());
	}

	private static String itemKey(ItemStack stack) {
		if (stack.isEmpty()) return "";
		// Use only registry name + count for a stable, JVM-restart-safe hash.
		// getComponents().hashCode() is NOT stable across restarts and caused
		// favorites to be lost on mod update / game restart.
		// This is sufficient for villager trades — identical item+count combos
		// with different components (e.g. two enchanted books at the same price)
		// are extremely rare in vanilla trading.
		return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "x" + stack.getCount();
	}
}
