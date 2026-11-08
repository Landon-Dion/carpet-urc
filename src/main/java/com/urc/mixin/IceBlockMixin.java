package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ice no longer melts from light (random tick); player destruction still leaves water. */
@Mixin(IceBlock.class)
public abstract class IceBlockMixin {
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void urc$noNaturalMelt(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (URCSettings.noIceMelting) {
			ci.cancel();
		}
	}
}
