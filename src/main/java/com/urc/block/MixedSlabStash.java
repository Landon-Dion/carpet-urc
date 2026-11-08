package com.urc.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Carries the two slab materials across a piston move.
 *
 * <p>Vanilla pistons refuse to move blocks that own a block entity. The mixed slab is allowed
 * through (see the piston mixins) and its state is stashed here when the source block entity is
 * removed; the moving block entity then restores it at the destination.</p>
 */
public final class MixedSlabStash {
	private static final Map<BlockPos, Entry> ENTRIES = new ConcurrentHashMap<>();

	private MixedSlabStash() {
	}

	public static void put(Level level, BlockPos pos, BlockState bottom, BlockState top) {
		BlockPos key = pos.immutable();
		ENTRIES.put(key, new Entry(level, bottom, top, level.getGameTime()));
		prune(level);
	}

	public static Entry take(Level level, BlockPos pos) {
		Entry entry = ENTRIES.remove(pos);

		if (entry != null && entry.level() == level) {
			return entry;
		}

		return null;
	}

	private static void prune(Level level) {
		long now = level.getGameTime();

		for (Map.Entry<BlockPos, Entry> mapEntry : ENTRIES.entrySet()) {
			Entry entry = mapEntry.getValue();

			if (entry.level() != level || now - entry.gameTime() > 40L) {
				ENTRIES.remove(mapEntry.getKey());
			}
		}

		// Safety valve: a piston move always resolves within a few ticks.
		if (ENTRIES.size() > 256) {
			ENTRIES.clear();
		}
	}

	public record Entry(Level level, BlockState bottom, BlockState top, long gameTime) {
	}
}
