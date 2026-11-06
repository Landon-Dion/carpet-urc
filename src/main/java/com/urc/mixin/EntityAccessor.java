package com.urc.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for {@code Entity.invulnerableTime}.
 *
 * <p>The field is public in 26.1/26.2 but private in 26.3; an accessor mixin keeps the source
 * compiling against every 26.x jar instead of touching the field directly.</p>
 */
@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("invulnerableTime")
	void urc$setInvulnerableTime(int value);

	@Accessor("invulnerableTime")
	int urc$getInvulnerableTime();
}
