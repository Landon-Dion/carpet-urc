package com.urc.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HopperBlockEntity.class)
public interface HopperAccessor {
	@Accessor("cooldownTime")
	void urc$setCooldownTime(int value);

	@Accessor("cooldownTime")
	int urc$getCooldownTime();
}
