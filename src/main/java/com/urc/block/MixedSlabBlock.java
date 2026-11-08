package com.urc.block;

import com.urc.URCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A full block made of two different slabs (top material + bottom material).
 *
 * <p>The block itself is invisible; {@link com.urc.client.MixedSlabRenderer} draws the two halves
 * with their own models. See {@code MixedSlabBlockEntity} for the stored materials.</p>
 */
public class MixedSlabBlock extends Block implements EntityBlock {
	public MixedSlabBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MixedSlabBlockEntity(pos, state);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		@Nullable BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

		if (blockEntity instanceof MixedSlabBlockEntity mixedSlab) {
			List<ItemStack> drops = new ArrayList<>(2);
			drops.add(new ItemStack(mixedSlab.getBottom().getBlock().asItem()));
			drops.add(new ItemStack(mixedSlab.getTop().getBlock().asItem()));
			return drops;
		}

		return super.getDrops(state, params);
	}
}
