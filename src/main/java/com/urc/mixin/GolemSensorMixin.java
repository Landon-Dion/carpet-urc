package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Makes villagers notice nearby iron golems every 5 ticks instead of every 200.
 *
 * <p>{@code GolemSensor()} defaults to a 200 tick scan; while a golem is nearby the sensor sets
 * {@code GOLEM_DETECTED_RECENTLY}, which is what stops villagers from wanting to spawn another
 * one. Scanning every 5 ticks keeps that memory fresh and lets farms react quickly.</p>
 */
@Mixin(GolemSensor.class)
public abstract class GolemSensorMixin {
	// The handler must be static: it is called from the constructor before super(),
	// where "this" is still uninitialized and cannot be passed to an instance method.
	@ModifyConstant(method = "<init>()V", constant = @Constant(intValue = 200))
	private static int urc$fastGolemScan(int original) {
		return URCSettings.efficientIronFarm ? 5 : original;
	}

	/**
	 * After every successful spawn the villagers get a {@code GOLEM_DETECTED_RECENTLY} memory for
	 * 599 ticks (~30 s), and that memory alone blocks the next spawn even if the golem dies
	 * instantly. Shorten it so the farm is only limited while a golem is actually nearby (the
	 * 5 tick sensor re-arms the memory in that case).
	 */
	@ModifyConstant(method = "golemDetected", constant = @Constant(longValue = 599L))
	private static long urc$shortGolemMemory(long original) {
		return URCSettings.efficientIronFarm ? 20L : original;
	}
}
