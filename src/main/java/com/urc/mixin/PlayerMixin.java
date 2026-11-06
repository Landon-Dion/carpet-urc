package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the vanilla attack cooldown while {@code noAttackCooldown} is enabled.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
	/**
	 * Vanilla scales damage, crit eligibility, sweep and knockback by
	 * {@code 0.2 + 0.8 * scale^2}. Returning a full charge for every hit removes the
	 * cooldown in one place, on both the client and the server.
	 */
	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	private void urc$alwaysFullAttackStrength(float baseTime, CallbackInfoReturnable<Float> cir) {
		if (URCSettings.noAttackCooldown) {
			cir.setReturnValue(1.0F);
		}
	}

	/**
	 * The server rejects a hit whose progress since the previous one is below the item's
	 * {@code minimum_attack_charge} (checked with a 5 tick tolerance when handling
	 * {@code ServerboundAttackPacket}). Bypass that gate as well, otherwise items with a
	 * charge requirement would still be throttled.
	 */
	@Inject(method = "cannotAttackWithItem", at = @At("HEAD"), cancellable = true)
	private void urc$ignoreMinimumAttackCharge(ItemStack stack, int tolerance, CallbackInfoReturnable<Boolean> cir) {
		if (URCSettings.noAttackCooldown) {
			cir.setReturnValue(false);
		}
	}
}
