package com.urc.mixin;

import com.urc.URCBlocks;
import com.urc.URCSettings;
import com.urc.block.MixedSlabBlockEntity;
import com.urc.block.MixedSlabStash;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores the two slab materials after a piston finished moving a mixed slab. */
@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	private static void urc$restoreMovedMixedSlab(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity movingEntity, CallbackInfo ci) {
		if (!URCSettings.mixedSlab || level.isClientSide()) {
			return;
		}

		if (level.getBlockState(pos).getBlock() != URCBlocks.MIXED_SLAB) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);

		if (!(blockEntity instanceof MixedSlabBlockEntity mixedSlab) || !mixedSlab.isEmpty()) {
			return;
		}

		BlockPos source = pos.relative(movingEntity.getPushDirection().getOpposite());
		MixedSlabStash.Entry entry = MixedSlabStash.take(level, source);

		if (entry != null) {
			mixedSlab.setHalves(entry.bottom(), entry.top());
		}
	}
}
