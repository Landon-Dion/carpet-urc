package com.urc.mixin;

import com.urc.PlacingBlockFlag;
import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placing a block against the face of an observer no longer makes the observer pulse.
 *
 * <p>Vanilla observers schedule a tick from {@code updateShape} whenever the neighbour in their
 * facing direction changes. While a player placement is in progress that scheduling is skipped, so
 * only real block state changes still trigger the observer.</p>
 */
@Mixin(ObserverBlock.class)
public abstract class ObserverBlockMixin {
	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void urc$skipPlacementPulse(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random,
			CallbackInfoReturnable<BlockState> cir) {
		if (!URCSettings.simpleObserverPlacement || !PlacingBlockFlag.isPlacing()) {
			return;
		}

		if (state.getValue(ObserverBlock.FACING) == directionToNeighbour && !state.getValue(ObserverBlock.POWERED)) {
			cir.setReturnValue(state);
		}
	}
}
