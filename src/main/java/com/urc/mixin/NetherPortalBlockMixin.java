package com.urc.mixin;

import com.urc.URCSettings;
import com.urc.portal.CustomPortalFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps custom-shaped portal blocks alive.
 *
 * <p>{@code NetherPortalBlock#updateShape} deletes itself unless the neighbouring block is also a
 * portal or {@code PortalShape.findAnyShape(...)} reports a complete rectangle. Since our portals
 * are not rectangles, any neighbour update (a block placed next to the portal, an explosion, ...)
 * would erase them. Here the portal block survives as long as its plane is still a closed
 * obsidian loop, and vanilla still removes it once the frame is actually broken.</p>
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void urc$keepCustomShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random,
			CallbackInfoReturnable<BlockState> cir) {
		if (!URCSettings.customNetherPortal) {
			return;
		}

		// The block stores the span axis; the plane it lives in is normal to the other one.
		Direction.Axis planeAxis = CustomPortalFrame.spanAxis(state.getValue(NetherPortalBlock.AXIS));

		if (CustomPortalFrame.isComplete(level, pos, planeAxis)) {
			// Block#updateShape simply returns the unchanged state.
			cir.setReturnValue(state);
		}
	}
}
