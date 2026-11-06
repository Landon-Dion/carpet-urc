package com.urc.portal;

import com.urc.URC;
import com.urc.URCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

import org.jetbrains.annotations.Nullable;

/**
 * A nether portal lying flat on the ground.
 *
 * <p>Vanilla's {@code NetherPortalBlock.AXIS} only has X and Z, so a horizontal portal cannot be
 * expressed as a block state of the vanilla block. This block is the horizontal counterpart: it
 * fills a flat obsidian loop, renders as a thin slab and reuses the vanilla teleport logic.</p>
 */
public class HorizontalNetherPortalBlock extends Block implements Portal {
	public HorizontalNetherPortalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (entity.canUsePortal(false)) {
			entity.setAsInsidePortal(this, pos);
		}
	}

	@Override
	public @Nullable TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos portalEntryPos) {
		// Vanilla picks the exit portal and creates a normal (vertical) one when needed; the source
		// block has no AXIS property, so vanilla falls back to its default axis.
		return ((Portal) Blocks.NETHER_PORTAL).getPortalDestination(level, entity, portalEntryPos);
	}

	@Override
	public int getPortalTransitionTime(ServerLevel level, Entity entity) {
		return ((Portal) Blocks.NETHER_PORTAL).getPortalTransitionTime(level, entity);
	}

	@Override
	public Portal.Transition getLocalTransition() {
		return Portal.Transition.CONFUSION;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (neighbourState.is(this) || CustomPortalFrame.isHorizontalPortalComplete(level, pos)) {
			return state;
		}

		// The obsidian loop was broken: the portal goes away, exactly like a vanilla one.
		return Blocks.AIR.defaultBlockState();
	}
}
