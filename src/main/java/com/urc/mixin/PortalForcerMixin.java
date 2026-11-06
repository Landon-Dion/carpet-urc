package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the portal search radius.
 *
 * <p>Vanilla {@code findClosestPortalPosition} does {@code int radius = toNether ? 16 : 128}
 * and queries the portal points of interest in a square of that size. The 128 (overworld side)
 * is replaced by the {@code netherPortalSearchRadius} rule so players can link portals across a
 * larger distance instead of getting a brand new portal. The nether-side 16 stays vanilla.</p>
 *
 * <p>Lithium's {@code fast_portals} module {@code @Overwrite}s the same method at the default
 * priority 1000. Mixin refuses to inject into a method merged by another mixin unless this
 * mixin has a strictly higher priority, so apply after Lithium (2000) and modify its copy of
 * the method, which still contains the same {@code 128} constant.</p>
 */
@Mixin(value = PortalForcer.class, priority = 2000)
public abstract class PortalForcerMixin {
	@ModifyConstant(method = "findClosestPortalPosition", constant = @Constant(intValue = 128))
	private int urc$searchRadius(int original) {
		return URCSettings.netherPortalSearchRadius;
	}
}
