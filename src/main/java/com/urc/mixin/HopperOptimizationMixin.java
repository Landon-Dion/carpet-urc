package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla hopper/chest micro-optimizations.
 *
 * <p>Two costs dominate vanilla hoppers: the 54-slot flat array of a double chest is rebuilt on
 * every {@code getSlots} call, and an idle hopper retries a full container/entity scan every tick
 * because the 8 tick cooldown is only set after a successful move.</p>
 *
 * <p>The 54-slot array is always cached (no behaviour change). The optional idle cooldown is
 * controlled by {@code hopperIdleCooldown}; it is off by default because it delays item pickup by
 * up to {@code hopperIdleCooldown - 1} ticks. It is intended for large farms where the scan cost
 * matters more than sub-tick pickup latency.</p>
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperOptimizationMixin {
	@Unique
	private static int[] urc$slots54;

	@Redirect(
			method = "getSlots",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;createFlatSlots(I)[I"
			)
	)
	private static int[] urc$cachedFlatSlots(int size) {
		if (size == 54) {
			int[] cached = urc$slots54;

			if (cached == null) {
				cached = urc$newFlatSlots(54);
				urc$slots54 = cached;
			}

			return cached;
		}

		return urc$newFlatSlots(size);
	}

	@Inject(method = "pushItemsTick", at = @At("TAIL"))
	private static void urc$throttleIdleHopper(Level level, BlockPos pos, BlockState state,
			HopperBlockEntity hopper, CallbackInfo ci) {
		if (URCSettings.hopperIdleCooldown <= 0 || level.isClientSide()) {
			return;
		}

		HopperAccessor accessor = (HopperAccessor) hopper;

		// 0 means this tick did not move anything and vanilla would retry next tick.
		if (accessor.urc$getCooldownTime() == 0) {
			accessor.urc$setCooldownTime(URCSettings.hopperIdleCooldown);
		}
	}

	@Unique
	private static int[] urc$newFlatSlots(int size) {
		int[] slots = new int[size];

		for (int i = 0; i < size; i++) {
			slots[i] = i;
		}

		return slots;
	}
}
