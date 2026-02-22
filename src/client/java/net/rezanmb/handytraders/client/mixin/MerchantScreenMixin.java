package net.rezanmb.handytraders.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.rezanmb.handytraders.client.TradeFavorites;
import net.rezanmb.handytraders.client.TradeHash;
import net.rezanmb.handytraders.config.HandyTradersConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

	@Shadow private int shopItem;
	@Shadow int scrollOff;

	@Unique private UUID handytraders$villagerUUID;

	@Unique private static final int STAR_CLICK_WIDTH = 12;
	@Unique private static final int BUTTON_X_OFFSET = 5;
	@Unique private static final int BUTTON_Y_OFFSET = 18;
	@Unique private static final int BUTTON_WIDTH = 88;
	@Unique private static final int BUTTON_HEIGHT = 20;
	@Unique private static final int VISIBLE_BUTTONS = 7;

	protected MerchantScreenMixin(MerchantMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void handytraders$captureVillager(CallbackInfo ci) {
		Entity target = Minecraft.getInstance().crosshairPickEntity;
		if (target != null) {
			this.handytraders$villagerUUID = target.getUUID();
		}
	}

	@Inject(method = "renderContents", at = @At("TAIL"))
	private void handytraders$renderFavorites(GuiGraphics guiGraphics, int mouseX, int mouseY,
											  float partialTick, CallbackInfo ci) {
		if (handytraders$villagerUUID == null) return;
		if (!HandyTradersConfig.get().enableFavorites) return;

		MerchantOffers offers = this.menu.getOffers();
		if (offers.isEmpty()) return;

		int buttonX = this.leftPos + BUTTON_X_OFFSET;
		int buttonStartY = this.topPos + BUTTON_Y_OFFSET;
		boolean showStar = HandyTradersConfig.get().showStarButton;

		for (int i = 0; i < VISIBLE_BUTTONS; i++) {
			int offerIndex = i + this.scrollOff;
			if (offerIndex >= offers.size()) break;

			MerchantOffer offer = offers.get(offerIndex);
			String tradeHash = TradeHash.hash(offer);
			boolean isFavorite = TradeFavorites.isFavorite(handytraders$villagerUUID, tradeHash);

			int buttonY = buttonStartY + i * BUTTON_HEIGHT;

			// Semi-transparent gold highlight on favorited trades
			if (isFavorite) {
				guiGraphics.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT,
						0x30FFD700);
			}

			// Star indicator
			if (showStar) {
				boolean isHovered = mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH
						&& mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;

				if (isFavorite) {
					guiGraphics.drawString(this.font, "\u2605", buttonX + 2, buttonY + 6,
							0xFFFFD700, false);
				} else if (isHovered) {
					guiGraphics.drawString(this.font, "\u2606", buttonX + 2, buttonY + 6,
							0x60FFFFFF, false);
				}
			}
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void handytraders$onStarClick(MouseButtonEvent event, boolean bl,
										  CallbackInfoReturnable<Boolean> cir) {
		if (handytraders$villagerUUID == null) return;
		if (!HandyTradersConfig.get().enableFavorites) return;
		if (!HandyTradersConfig.get().showStarButton) return;
		if (event.button() != 0) return; // Left click only

		double mouseX = event.x();
		double mouseY = event.y();
		MerchantOffers offers = this.menu.getOffers();
		int buttonX = this.leftPos + BUTTON_X_OFFSET;
		int buttonStartY = this.topPos + BUTTON_Y_OFFSET;

		for (int i = 0; i < VISIBLE_BUTTONS; i++) {
			int offerIndex = i + this.scrollOff;
			if (offerIndex >= offers.size()) break;

			int buttonY = buttonStartY + i * BUTTON_HEIGHT;

			if (mouseX >= buttonX && mouseX < buttonX + STAR_CLICK_WIDTH
					&& mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {
				MerchantOffer offer = offers.get(offerIndex);
				String tradeHash = TradeHash.hash(offer);
				TradeFavorites.toggleFavorite(handytraders$villagerUUID, tradeHash);
				TradeFavorites.save();

				if (HandyTradersConfig.get().playSounds) {
					Minecraft.getInstance().player.playSound(
							SoundEvents.AMETHYST_BLOCK_CHIME, 0.3F, 1.2F);
				}

				cir.setReturnValue(true);
				return;
			}
		}
	}
}
