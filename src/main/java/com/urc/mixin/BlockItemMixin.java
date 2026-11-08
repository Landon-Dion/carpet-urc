package com.urc.mixin;

import com.urc.PlacingBlockFlag;
import com.urc.URCBlocks;
import com.urc.URCSettings;
import com.urc.block.MixedSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Combining two different slab items into one mixed slab.
 *
 * <p>Right-clicking a single slab with a different slab item replaces the block with
 * {@link URCBlocks#MIXED_SLAB}, keeping the clicked slab as one half and the held one as the other.
 * The renderer draws both halves.</p>
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Inject(method = "place", at = @At("HEAD"))
	private void urc$beginPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (URCSettings.simpleObserverPlacement) {
			PlacingBlockFlag.begin();
		}
	}

	@Inject(method = "place", at = @At("RETURN"))
	private void urc$endPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		PlacingBlockFlag.end();
	}

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void urc$combineSlabs(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (!URCSettings.mixedSlab) {
			return;
		}

		Player player = context.getPlayer();

		if (player == null) {
			return;
		}

		ItemStack held = context.getItemInHand();

		if (!(held.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof SlabBlock placedSlab)) {
			return;
		}

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState clicked = level.getBlockState(pos);

		if (!(clicked.getBlock() instanceof SlabBlock clickedSlab) || clicked.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
			return;
		}

		if (clickedSlab == placedSlab) {
			return;
		}

		boolean clickedBottom = clicked.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		BlockState clickedNormal = clicked.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		BlockState placedNormal = placedSlab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		BlockState bottom = clickedBottom ? clickedNormal : placedNormal;
		// The two halves are stored with their real slab type: the renderer draws the stored state
		// directly, so the upper half must be type=top to appear in the upper half of the block.
		BlockState top = (clickedBottom ? placedNormal : clickedNormal).setValue(SlabBlock.TYPE, SlabType.TOP);

		if (!level.isClientSide()) {
			level.setBlock(pos, URCBlocks.MIXED_SLAB.defaultBlockState(), 3);

			if (level.getBlockEntity(pos) instanceof MixedSlabBlockEntity mixedSlab) {
				mixedSlab.setHalves(bottom, top);
			}

			level.playSound(null, pos, SoundType.STONE.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
		}

		if (!player.getAbilities().instabuild) {
			held.shrink(1);
		}

		cir.setReturnValue(InteractionResult.SUCCESS);
	}
}
