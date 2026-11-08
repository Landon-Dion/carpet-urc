package com.urc.mixin;

import com.urc.URCBlocks;
import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allows pistons to push the mixed slab even though it owns a block entity. */
@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {
	@Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
	private static void urc$allowMixedSlab(BlockState state, Level level, BlockPos pos, Direction direction,
			boolean allowDestroyable, Direction connectionDirection, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && URCSettings.mixedSlab && state.getBlock() == URCBlocks.MIXED_SLAB) {
			cir.setReturnValue(true);
		}
	}
}
