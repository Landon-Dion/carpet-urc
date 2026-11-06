package com.urc;

import com.urc.portal.HorizontalNetherPortalBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Blocks added by URC. */
public final class URCBlocks {
	private static final ResourceKey<Block> HORIZONTAL_NETHER_PORTAL_ID = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(URC.MOD_ID, "horizontal_nether_portal"));

	public static final Block HORIZONTAL_NETHER_PORTAL = new HorizontalNetherPortalBlock(
			BlockBehaviour.Properties.of()
					// Since 1.21.2 the block id is part of the properties and must be set before
					// the block is constructed, otherwise BlockBehaviour#effectiveDrops throws.
					.setId(HORIZONTAL_NETHER_PORTAL_ID)
					.noCollision()
					.noOcclusion()
					.strength(-1.0f)
					.sound(SoundType.GLASS)
					.lightLevel(state -> 11)
					);

	private URCBlocks() {
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.BLOCK,
				HORIZONTAL_NETHER_PORTAL_ID,
				HORIZONTAL_NETHER_PORTAL);
	}
}
