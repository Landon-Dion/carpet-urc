package com.urc;

import com.urc.block.MixedSlabBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class URCBlockEntities {
	public static final BlockEntityType<MixedSlabBlockEntity> MIXED_SLAB = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			Identifier.fromNamespaceAndPath("urc", "mixed_slab"),
			FabricBlockEntityTypeBuilder.create(MixedSlabBlockEntity::new, URCBlocks.MIXED_SLAB).build()
	);

	private URCBlockEntities() {
	}

	public static void register() {
		// Static initialiser performs the registration.
	}
}
