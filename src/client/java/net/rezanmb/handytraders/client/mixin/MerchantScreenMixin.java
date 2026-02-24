package net.rezanmb.handytraders.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

	@Shadow private int shopItem;
	@Shadow int scrollOff;
	@Unique private UUID handytraders$villagerUUID;
	@Unique private boolean handytraders$needsSort = true;
	/** Maps sorted index -> original server index. Null when no reordering active. */
	@Unique private int[] handytraders$sortedToActual;
	/** Snapshot of the server's original offer order, captured before first sort. */
	@Unique private List<MerchantOffer> handytraders$originalOffers;
	/** Tracks the MerchantOffers reference to detect server-side replacements (restock/level-up). */
	@Unique private MerchantOffers handytraders$lastKnownOffers;

	@Unique private static final int BOOKMARK_SIZE = 7;
	@Unique private static final int BOOKMARK_INSET = 1;
	@Unique private static final int BUTTON_X_OFFSET = 5;
	@Unique private static final int BUTTON_Y_OFFSET = 17;
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
		this.handytraders$needsSort = true;
		this.handytraders$originalOffers = null;
		this.handytraders$lastKnownOffers = null;
	}

	// -- Sort favorites to top --

	@Inject(method = "renderContents", at = @At("HEAD"))
	private void handytraders$sortIfNeeded(GuiGraphics guiGraphics, int mouseX, int mouseY,
										   float partialTick, CallbackInfo ci) {
		// Detect server-side offer replacement (restock, level-up, etc.)
		MerchantOffers currentOffers = this.menu.getOffers();
		if (handytraders$lastKnownOffers != null && currentOffers != handytraders$lastKnownOffers) {
			handytraders$originalOffers = null;
			handytraders$needsSort = true;
		}
		handytraders$lastKnownOffers = currentOffers;

		if (!handytraders$needsSort) return;
		handytraders$needsSort = false;

		// On a re-sort (bookmark toggle or offer refresh), track which actual trade
		// was selected so we can preserve the selection across the reorder.
		boolean isResort = handytraders$sortedToActual != null;
		int oldActualIndex = -1;
		if (isResort && shopItem >= 0 && shopItem < handytraders$sortedToActual.length) {
			oldActualIndex = handytraders$sortedToActual[shopItem];
		}

		handytraders$sortOffers();

		if (isResort && oldActualIndex >= 0) {
			// Find where the previously selected trade moved in the new sort order
			if (handytraders$sortedToActual != null) {
				for (int i = 0; i < handytraders$sortedToActual.length; i++) {
					if (handytraders$sortedToActual[i] == oldActualIndex) {
						shopItem = i;
						break;
					}
				}
			} else {
				// No favorites — offers restored to original order
				shopItem = oldActualIndex;
			}
			// Update the container's selection hint so the result slot stays correct.
			// Do NOT call postButtonClick() here — it sends a network packet and moves
			// items during rendering, causing race conditions with the server that
			// make items flash in and out of payment slots.
			this.menu.setSelectionHint(shopItem);
		}
		// On initial sort (screen open): shopItem stays at 0, which selects the
		// first trade in sorted order (the top favorite). No setSelectionHint needed
		// since the container's default selectionHint is also 0.
	}

	@Unique
	private void handytraders$sortOffers() {
		MerchantOffers offers = this.menu.getOffers();
		if (offers.isEmpty()) {
			handytraders$sortedToActual = null;
			return;
		}

		// Capture the server's original order on first sort (or after server refresh)
		if (handytraders$originalOffers == null || handytraders$originalOffers.size() != offers.size()) {
			handytraders$originalOffers = new ArrayList<>(offers);
		}

		if (handytraders$villagerUUID == null || !HandyTradersConfig.get().enableFavorites) {
			// Restore original order
			offers.clear();
			offers.addAll(handytraders$originalOffers);
			handytraders$sortedToActual = null;
			return;
		}

		// Always sort from the original server order
		List<Integer> favoriteIndices = new ArrayList<>();
		List<Integer> otherIndices = new ArrayList<>();

		for (int i = 0; i < handytraders$originalOffers.size(); i++) {
			String hash = TradeHash.hash(handytraders$originalOffers.get(i));
			if (TradeFavorites.isFavorite(handytraders$villagerUUID, hash)) {
				favoriteIndices.add(i);
			} else {
				otherIndices.add(i);
			}
		}

		// No favorites — restore original order
		if (favoriteIndices.isEmpty()) {
			offers.clear();
			offers.addAll(handytraders$originalOffers);
			handytraders$sortedToActual = null;
			return;
		}

		// Build mapping and reorder from the original
		List<Integer> sortedOrder = new ArrayList<>();
		sortedOrder.addAll(favoriteIndices);
		sortedOrder.addAll(otherIndices);

		handytraders$sortedToActual = sortedOrder.stream().mapToInt(Integer::intValue).toArray();

		offers.clear();
		for (int idx : handytraders$sortedToActual) {
			offers.add(handytraders$originalOffers.get(idx));
		}
	}

	/**
	 * Intercept postButtonClick to remap the trade index in the server-bound packet
	 * from sorted order back to the server's original order.
	 *
	 * We replace the entire method rather than using @ModifyArg because the
	 * @ModifyArg on ServerboundSelectTradePacket.<init> doesn't fire at runtime
	 * (likely a Mixin/mapping issue with constructor argument modification).
	 */
	@Inject(method = "postButtonClick", at = @At("HEAD"), cancellable = true)
	private void handytraders$remapPostButtonClick(CallbackInfo ci) {
		if (handytraders$sortedToActual == null) {
			// No sorting active — let vanilla handle it unmodified
			return;
		}

		// Do what vanilla postButtonClick does:
		// 1. setSelectionHint uses sorted index (correct for sorted client offers)
		this.menu.setSelectionHint(this.shopItem);
		// 2. tryMoveItems uses sorted index (correct for sorted client offers)
		this.menu.tryMoveItems(this.shopItem);
		// 3. Send packet with ACTUAL (original) index so the server selects the right trade
		int actualIndex = this.shopItem;
		if (this.shopItem >= 0 && this.shopItem < handytraders$sortedToActual.length) {
			actualIndex = handytraders$sortedToActual[this.shopItem];
		}
		Minecraft.getInstance().getConnection().send(new ServerboundSelectTradePacket(actualIndex));

		ci.cancel();
	}

	// -- Render favorites overlay --

	@Inject(method = "renderContents", at = @At("TAIL"))
	private void handytraders$renderFavorites(GuiGraphics guiGraphics, int mouseX, int mouseY,
											  float partialTick, CallbackInfo ci) {
		if (handytraders$villagerUUID == null) return;
		if (!HandyTradersConfig.get().enableFavorites) return;

		MerchantOffers offers = this.menu.getOffers();
		if (offers.isEmpty()) return;

		int buttonX = this.leftPos + BUTTON_X_OFFSET;
		int buttonStartY = this.topPos + BUTTON_Y_OFFSET;

		for (int i = 0; i < VISIBLE_BUTTONS; i++) {
			int offerIndex = i + this.scrollOff;
			if (offerIndex >= offers.size()) break;

			MerchantOffer offer = offers.get(offerIndex);
			String tradeHash = TradeHash.hash(offer);
			boolean isFavorite = TradeFavorites.isFavorite(handytraders$villagerUUID, tradeHash);

			int buttonY = buttonStartY + i * BUTTON_HEIGHT;
			int cornerX = buttonX + BOOKMARK_INSET;
			int cornerY = buttonY + BOOKMARK_INSET + 1;
			int hitSize = BOOKMARK_SIZE + BOOKMARK_INSET + 1;
			boolean isCornerHovered = mouseX >= buttonX && mouseX < buttonX + hitSize
					&& mouseY >= buttonY && mouseY < buttonY + hitSize;

			if (isFavorite) {
				int fill = isCornerHovered ? 0xFFFFE850 : 0xFFDAA520;
				int highlight = isCornerHovered ? 0xFFFFFF80 : 0xFFFFD700;
				int shadow = isCornerHovered ? 0xFFC89020 : 0xFF8B6914;
				handytraders$drawBookmarkCorner(guiGraphics, cornerX, cornerY,
						fill, highlight, shadow);
			} else if (isCornerHovered) {
				handytraders$drawBookmarkCorner(guiGraphics, cornerX, cornerY,
						0x50FFD700, 0x70FFE850, 0x50B8860B);
			}
		}
	}

	@Unique
	private void handytraders$drawBookmarkCorner(GuiGraphics g, int x, int y,
												 int fillColor, int highlightColor, int shadowColor) {
		// Fill the triangle body
		for (int row = 0; row < BOOKMARK_SIZE; row++) {
			int width = BOOKMARK_SIZE - row;
			g.fill(x, y + row, x + width, y + row + 1, fillColor);
		}
		// Highlight: top edge
		g.fill(x, y, x + BOOKMARK_SIZE, y + 1, highlightColor);
		// Highlight: left edge
		g.fill(x, y, x + 1, y + BOOKMARK_SIZE, highlightColor);
		// Shadow: diagonal hypotenuse
		for (int i = 0; i < BOOKMARK_SIZE; i++) {
			g.fill(x + BOOKMARK_SIZE - 1 - i, y + i,
					x + BOOKMARK_SIZE - i, y + i + 1, shadowColor);
		}
	}

	// -- Bookmark click handling --

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void handytraders$onBookmarkClick(MouseButtonEvent event, boolean bl,
											  CallbackInfoReturnable<Boolean> cir) {
		if (handytraders$villagerUUID == null) return;
		if (!HandyTradersConfig.get().enableFavorites) return;
		if (event.button() != 0) return;

		double mouseX = event.x();
		double mouseY = event.y();
		MerchantOffers offers = this.menu.getOffers();
		int buttonX = this.leftPos + BUTTON_X_OFFSET;
		int buttonStartY = this.topPos + BUTTON_Y_OFFSET;

		for (int i = 0; i < VISIBLE_BUTTONS; i++) {
			int offerIndex = i + this.scrollOff;
			if (offerIndex >= offers.size()) break;

			int buttonY = buttonStartY + i * BUTTON_HEIGHT;

			int hitSize = BOOKMARK_SIZE + BOOKMARK_INSET + 1;
			if (mouseX >= buttonX && mouseX < buttonX + hitSize
					&& mouseY >= buttonY && mouseY < buttonY + hitSize) {
				MerchantOffer offer = offers.get(offerIndex);
				String tradeHash = TradeHash.hash(offer);
				TradeFavorites.toggleFavorite(handytraders$villagerUUID, tradeHash);
				TradeFavorites.save();

				// Trigger re-sort on next frame
				handytraders$needsSort = true;

				Minecraft.getInstance().player.playSound(
						SoundEvents.AMETHYST_BLOCK_CHIME, 0.3F, 1.2F);

				cir.setReturnValue(true);
				return;
			}
		}
	}
}
