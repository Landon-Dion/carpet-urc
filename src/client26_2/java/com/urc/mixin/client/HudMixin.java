package com.urc.mixin.client;

import com.urc.URCSettings;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides the attack cooldown indicator in the HUD while {@code noAttackCooldown} is enabled.
 *
 * <p>26.2 moved the HUD out of {@code net.minecraft.client.gui.Gui} into
 * {@code net.minecraft.client.gui.Hud}; this is the 26.2+ counterpart of {@code GuiMixin}
 * (compiled only for 26.2/26.3 builds). Returning a full charge is not enough - the HUD draws the
 * "fully charged" marker as soon as the scale reaches 1.0 while a living entity is under the
 * crosshair, so the progress bar and the full marker both have to be disabled.</p>
 */
@Mixin(Hud.class)
public abstract class HudMixin {
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
