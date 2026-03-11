package net.rezanmb.handytraders.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

/**
 * Config screen stub — YACL is not yet available for Minecraft 26.1.
 * This class is kept so ModMenuIntegration can reference it once YACL is ported.
 */
@Environment(EnvType.CLIENT)
public class HandyTradersConfigScreen {

	public static Screen create(Screen parent) {
		// YACL not available for 26.1 yet — return null to indicate no config screen
		return null;
	}
}
