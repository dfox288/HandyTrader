package net.rezanmb.handytraders.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Computes a stable hash for a MerchantOffer based on item IDs, counts,
 * and component data. This allows identifying the same trade across sessions
 * even if the trade index shifts when a villager levels up.
 */
@Environment(EnvType.CLIENT)
public final class TradeHash {

	private TradeHash() {}

	public static String hash(MerchantOffer offer) {
		String key = itemKey(offer.getBaseCostA()) + "|" +
				itemKey(offer.getCostB()) + "|" +
				itemKey(offer.getResult());
		return Long.toHexString(key.hashCode() & 0xFFFFFFFFL);
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
