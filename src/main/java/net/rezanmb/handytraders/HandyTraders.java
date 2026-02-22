package net.rezanmb.handytraders;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyTraders implements ModInitializer {
	public static final String MOD_ID = "handytraders";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Handy Traders initialized");
	}
}
