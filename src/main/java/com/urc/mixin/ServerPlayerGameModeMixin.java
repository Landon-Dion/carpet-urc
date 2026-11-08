package com.urc.mixin;

import com.urc.URCBlockEntities;
import com.urc.URCBlocks;
import com.urc.URCSettings;
import com.urc.block.MixedSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Slab splitting: breaking a double slab (or a mixed slab) only destroys the aimed half.
 *
 * <p>The removed half is dropped as its own slab item and the block stays in the world as the
 * other half, instead of the whole block vanishing.</p>
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
	@Shadow
	protected ServerLevel level;

	@Shadow
	@Final
	protected ServerPlayer player;

	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void urc$breakHalfSlab(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState state = this.level.getBlockState(pos);

		if (state.getBlock() == URCBlocks.MIXED_SLAB) {
			this.urc$breakMixedSlab(pos, state, cir);
			return;
		}

		if (!URCSettings.halfSlabBreak) {
			return;
		}

		if (!(state.getBlock() instanceof SlabBlock) || state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
			return;
		}

		boolean topHalf = this.urc$aimsTopHalf(pos);
		SlabType keep = topHalf ? SlabType.BOTTOM : SlabType.TOP;

		this.level.setBlock(pos, state.setValue(SlabBlock.TYPE, keep), 3);
		this.level.levelEvent(2001, pos, Block.getId(state));
		Block.popResource(this.level, pos, new ItemStack(state.getBlock().asItem()));
		cir.setReturnValue(true);
	}

	private void urc$breakMixedSlab(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!URCSettings.mixedSlab || !(this.level.getBlockEntity(pos) instanceof MixedSlabBlockEntity mixedSlab)) {
			return;
		}

		boolean topHalf = this.urc$aimsTopHalf(pos);
		BlockState removed = topHalf ? mixedSlab.getTop() : mixedSlab.getBottom();
		BlockState kept = topHalf ? mixedSlab.getBottom() : mixedSlab.getTop();
		SlabType keepType = topHalf ? SlabType.BOTTOM : SlabType.TOP;

		this.level.setBlock(pos, kept.setValue(SlabBlock.TYPE, keepType), 3);
		this.level.levelEvent(2001, pos, Block.getId(state));
		Block.popResource(this.level, pos, new ItemStack(removed.getBlock().asItem()));
		cir.setReturnValue(true);
	}

	private boolean urc$aimsTopHalf(BlockPos pos) {
		Vec3 eye = this.player.getEyePosition();
		Vec3 end = eye.add(this.player.getViewVector(1.0F).scale(6.0));
		return new AABB(pos).clip(eye, end)
				.map(hit -> hit.y > pos.getY() + 0.5)
				.orElse(this.player.getEyeY() > pos.getY() + 0.5);
	}
}
