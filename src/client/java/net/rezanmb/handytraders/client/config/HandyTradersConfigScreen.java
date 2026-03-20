package net.rezanmb.handytraders.client.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.rezanmb.handytraders.config.HandyTradersConfig;

@Environment(EnvType.CLIENT)
public class HandyTradersConfigScreen {

	public static Screen create(Screen parent) {
		HandyTradersConfig config = HandyTradersConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.handytraders.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handytraders.category.general"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handytraders.enableFavorites"))
								.description(OptionDescription.of(
										Component.translatable("config.handytraders.enableFavorites.desc")))
								.binding(true, () -> config.enableFavorites, val -> config.enableFavorites = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())
				.save(HandyTradersConfig::save)
				.build()
				.generateScreen(parent);
	}
}
