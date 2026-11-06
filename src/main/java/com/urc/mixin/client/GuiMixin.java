package com.urc.mixin.client;

import com.urc.URCSettings;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides the attack cooldown indicator in the HUD while {@code noAttackCooldown} is enabled.
 *
 * <p>Returning a full charge is not enough: the HUD draws the "fully charged" marker as soon as
 * the scale reaches 1.0 while a living entity is under the crosshair, so the only way to get rid
 * of the indicator is to make {@code extractCrosshair} draw neither branch. Forcing the scale
 * to 1 disables the progress bar, and forcing the item delay to 0 disables the full marker
 * ({@code renderMaxAttackIndicator = delay > 5}).</p>
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Redirect(
			method = "extractCrosshair",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F")
	)
	private float urc$hideCrosshairProgressBar(LocalPlayer player, float baseTime) {
		return URCSettings.noAttackCooldown ? 1.0F : player.getAttackStrengthScale(baseTime);
	}

	@Redirect(
			method = "extractCrosshair",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCurrentItemAttackStrengthDelay()F")
	)
	private float urc$hideCrosshairFullMarker(LocalPlayer player) {
		return URCSettings.noAttackCooldown ? 0.0F : player.getCurrentItemAttackStrengthDelay();
	}

	@Redirect(
			method = "extractItemHotbar",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F")
	)
	private float urc$hideHotbarIndicator(LocalPlayer player, float baseTime) {
		return URCSettings.noAttackCooldown ? 1.0F : player.getAttackStrengthScale(baseTime);
	}
}
