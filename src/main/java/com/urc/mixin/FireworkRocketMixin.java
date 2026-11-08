package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Unbounded elytra acceleration with firework rockets.
 *
 * <p>Vanilla pulls the player's velocity towards {@code look * 1.5}:
 * {@code v + 0.1*look + 0.5*(1.5*look - v)}, which is the effective speed cap. With the rule on
 * the interpolation is replaced by a plain {@code v + 0.1*look}, so every rocket tick adds speed
 * and repeated rockets keep stacking.</p>
 */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketMixin {
	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
					ordinal = 0
			)
	)
	private void urc$unboundedElytraBoost(LivingEntity rider, Vec3 vanilla) {
		if (!URCSettings.fastElytra) {
			rider.setDeltaMovement(vanilla);
			return;
		}

		rider.setDeltaMovement(rider.getDeltaMovement().add(rider.getLookAngle().scale(0.1)));
	}
}
