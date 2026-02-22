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
		StringBuilder sb = new StringBuilder();
		sb.append(BuiltInRegistries.ITEM.getKey(stack.getItem()));
		sb.append("x").append(stack.getCount());
		// Include component data hash to distinguish enchanted books, potions, etc.
		// getComponents() includes enchantments, stored enchantments, potion contents, etc.
		sb.append("#").append(stack.getComponents().hashCode());
		return sb.toString();
	}
}
