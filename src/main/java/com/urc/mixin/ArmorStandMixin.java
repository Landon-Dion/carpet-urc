package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While {@code noAttackCooldown} is enabled, armor stands ignore any damage caused by a
 * player: melee hits, player-fired arrows and other player-owned damage sources.
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void urc$ignorePlayerDamage(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		if (!URCSettings.noAttackCooldown) {
			return;
		}

		Entity attacker = source.getEntity();
		Entity direct = source.getDirectEntity();

		if (attacker instanceof Player || direct instanceof Player) {
			cir.setReturnValue(false);
		}
	}
}
