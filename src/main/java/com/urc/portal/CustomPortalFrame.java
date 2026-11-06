package com.urc.portal;

import com.urc.URC;
import com.urc.URCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Finds non-rectangular nether portal frames, vertical or horizontal.
 *
 * <p>Vanilla's {@code PortalShape} only accepts axis-aligned rectangles. Here any connected
 * region of empty blocks that is completely enclosed by obsidian inside one plane counts as a
 * frame, as long as both in-plane extents stay within 1..21 blocks.</p>
 */
public final class CustomPortalFrame {
	public static final int MIN_SIZE = 1;
	public static final int MAX_SIZE = 21;

	private static final int MAX_INTERIOR = MAX_SIZE * MAX_SIZE;

	/**
	 * Plane picked when the fire was lit, captured by {@code canBePlacedAt} and consumed by
	 * {@code onPlace} on the same thread, so both see the same decision.
	 */
	private static final ThreadLocal<Direction.Axis> PENDING_AXIS = new ThreadLocal<>();

	private CustomPortalFrame() {
	}

	public record Frame(List<BlockPos> interior, Direction.Axis axis, int width, int height) {
	}

	/** Mirrors vanilla: nether portals only exist on the overworld and nether dimensions. */
	public static boolean inPortalDimension(Level level) {
		return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
	}

	public static void clearPendingAxis() {
		PENDING_AXIS.remove();
	}

	public static void rememberPendingAxis(Direction.Axis axis) {
		PENDING_AXIS.set(axis);
	}

	public static Direction.Axis takePendingAxis() {
		Direction.Axis axis = PENDING_AXIS.get();
		PENDING_AXIS.remove();
		return axis;
	}

	public static Frame findEmptyFrame(LevelReader level, BlockPos origin, boolean allowVertical, boolean allowHorizontal) {
		return findEmptyFrame(level, origin, null, allowVertical, allowHorizontal);
	}

	/**
	 * The portal plane is the plane the obsidian loop lies in, never the player's view.
	 *
	 * <p>A free-standing frame only has one valid plane: the perpendicular plane runs through the
	 * opening and hits open air, failing the closed-loop test. Ambiguity only appears when the
	 * frame is embedded in solid obsidian (a perpendicular 1-wide slice closes too) or when a
	 * horizontal loop and a vertical one share the ignition block. The clicked face decides
	 * first; without it a genuine opening (width >= 2) beats a 1-wide slice, then the tighter
	 * loop wins, then X.</p>
	 */
	public static Frame findEmptyFrame(LevelReader level, BlockPos origin, Direction.Axis hint,
			boolean allowVertical, boolean allowHorizontal) {
		Frame xFrame = allowVertical ? scan(level, origin, Direction.Axis.X, CustomPortalFrame::isIgnitable) : null;
		Frame zFrame = allowVertical ? scan(level, origin, Direction.Axis.Z, CustomPortalFrame::isIgnitable) : null;
		Frame yFrame = allowHorizontal ? scan(level, origin, Direction.Axis.Y, CustomPortalFrame::isIgnitable) : null;

		Frame chosen = decide(xFrame, zFrame, yFrame, hint);

		if (level instanceof Level actualLevel && !actualLevel.isClientSide()) {
			URC.LOGGER.info("Custom portal probe at {} (hint {}): X={} Z={} Y={} -> {}",
					origin.toShortString(), hint, describe(xFrame), describe(zFrame), describe(yFrame),
					chosen == null ? "none" : chosen.axis());
		}

		return chosen;
	}

	private static Frame decide(Frame xFrame, Frame zFrame, Frame yFrame, Direction.Axis hint) {
		List<Frame> candidates = new ArrayList<>(3);

		if (xFrame != null) {
			candidates.add(xFrame);
		}

		if (zFrame != null) {
			candidates.add(zFrame);
		}

		if (yFrame != null) {
			candidates.add(yFrame);
		}

		if (candidates.isEmpty()) {
			return null;
		}

		if (candidates.size() == 1) {
			return candidates.get(0);
		}

		if (hint != null) {
			for (Frame candidate : candidates) {
				if (candidate.axis() != hint) {
					continue;
				}

				boolean hintedIsNarrow = candidate.width() < 2;
				boolean otherIsOpening = candidates.stream()
						.anyMatch(other -> other != candidate && other.width() >= 2);

				if (!(hintedIsNarrow && otherIsOpening)) {
					return candidate;
				}
			}
		}

		Frame best = null;

		for (Frame candidate : candidates) {
			if (best == null) {
				best = candidate;
				continue;
			}

			boolean candidateIsOpening = candidate.width() >= 2;
			boolean bestIsOpening = best.width() >= 2;

			if (candidateIsOpening != bestIsOpening) {
				if (candidateIsOpening) {
					best = candidate;
				}

				continue;
			}

			if (candidate.interior().size() < best.interior().size()) {
				best = candidate;
			}
		}

		return best;
	}

	/** True when {@code origin} is part of an enclosed vanilla portal block region. */
	public static boolean isComplete(LevelReader level, BlockPos origin, Direction.Axis planeAxis) {
		return scan(level, origin, planeAxis,
				state -> state.is(Blocks.NETHER_PORTAL)
						&& state.getValue(NetherPortalBlock.AXIS) == spanAxis(planeAxis)) != null;
	}

	/** True when {@code origin} is part of an enclosed horizontal portal region. */
	public static boolean isHorizontalPortalComplete(LevelReader level, BlockPos origin) {
		return scan(level, origin, Direction.Axis.Y,
				state -> state.is(URCBlocks.HORIZONTAL_NETHER_PORTAL)) != null;
	}

	public static boolean tryCreate(Level level, BlockPos origin, Direction.Axis hint,
			boolean allowVertical, boolean allowHorizontal) {
		Frame frame = findEmptyFrame(level, origin, hint, allowVertical, allowHorizontal);

		if (frame == null) {
			return false;
		}

		// NetherPortalBlock.AXIS is the horizontal axis the portal's *width* runs along, i.e. the
		// axis contained in the plane - not the plane's normal. A frame in the x = const plane
		// spans Z, so its blocks need AXIS=Z, otherwise the portal is built rotated 90 degrees.
		BlockState filled = frame.axis() == Direction.Axis.Y
				? URCBlocks.HORIZONTAL_NETHER_PORTAL.defaultBlockState()
				: Blocks.NETHER_PORTAL.defaultBlockState()
						.setValue(NetherPortalBlock.AXIS, spanAxis(frame.axis()));

		for (BlockPos pos : frame.interior()) {
			// Flag 18 (UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE) is what vanilla's PortalShape uses:
			// it syncs the blocks without triggering a neighbour shape-update cascade while the
			// region is still being filled.
			level.setBlock(pos, filled, 18);
		}

		return true;
	}

	private static boolean isIgnitable(BlockState state) {
		// Snow layers, tall grass, flowers and similar can sit inside a frame (a flat ring on the
		// ground is usually covered in snow); they are replaced when the portal is created.
		return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof BaseFireBlock;
	}

	private static Frame scan(LevelReader level, BlockPos origin, Direction.Axis axis, Predicate<BlockState> isPart) {
		if (!isPart.test(level.getBlockState(origin))) {
			return null;
		}

		Direction.Axis extentA = axis == Direction.Axis.Y ? Direction.Axis.X : horizontalAxis(axis);
		Direction.Axis extentB = axis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y;

		Set<BlockPos> region = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();

		region.add(origin);
		queue.add(origin);

		int minA = Integer.MAX_VALUE;
		int maxA = Integer.MIN_VALUE;
		int minB = Integer.MAX_VALUE;
		int maxB = Integer.MIN_VALUE;

		while (!queue.isEmpty()) {
			BlockPos pos = queue.poll();

			minA = Math.min(minA, pos.get(extentA));
			maxA = Math.max(maxA, pos.get(extentA));
			minB = Math.min(minB, pos.get(extentB));
			maxB = Math.max(maxB, pos.get(extentB));

			if (region.size() > MAX_INTERIOR) {
				return null;
			}

			for (Direction direction : Direction.values()) {
				// Traverse only the two in-plane axes.
				if (direction.getAxis() == axis) {
					continue;
				}

				BlockPos next = pos.relative(direction);

				if (region.contains(next)) {
					continue;
				}

				BlockState state = level.getBlockState(next);

				if (isPart.test(state)) {
					region.add(next);
					queue.add(next);
				} else if (!state.is(Blocks.OBSIDIAN)) {
					// Opening in the loop: not a closed frame.
					return null;
				}
			}
		}

		int width = maxA - minA + 1;
		int height = maxB - minB + 1;

		if (width < MIN_SIZE || width > MAX_SIZE || height < MIN_SIZE || height > MAX_SIZE) {
			return null;
		}

		return new Frame(List.copyOf(region), axis, width, height);
	}

	private static String describe(Frame frame) {
		return frame == null ? "none" : frame.width() + "x" + frame.height() + "(" + frame.interior().size() + ")";
	}

	/** Horizontal axis contained in the plane whose normal is {@code planeAxis}. */
	public static Direction.Axis spanAxis(Direction.Axis planeAxis) {
		return planeAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
	}

	private static Direction.Axis horizontalAxis(Direction.Axis axis) {
		return axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
	}
}
