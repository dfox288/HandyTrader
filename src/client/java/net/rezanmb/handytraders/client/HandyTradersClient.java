package net.rezanmb.handytraders.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HandyTradersClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TradeFavorites.load();
	}
}
