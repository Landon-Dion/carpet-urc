package com.urc.mixin.client;

import com.urc.URCBlocks;
import com.urc.URCSettings;
import com.urc.block.MixedSlabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
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
 * Removes the one-frame flicker when breaking a mixed slab.
 *
 * <p>{@code destroyBlock} removes the block locally before the server answers, so the player sees
 * air for one round trip and then the remaining half. This restores the surviving half locally
 * right after the prediction, so the visual never changes.</p>
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	private BlockState urc$predictedRemainder;

	@Inject(method = "destroyBlock", at = @At("HEAD"))
	private void urc$captureRemainder(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		this.urc$predictedRemainder = null;

		if (!URCSettings.mixedSlab || this.minecraft.level == null || this.minecraft.player == null) {
			return;
		}

		if (this.minecraft.level.getBlockState(pos).getBlock() != URCBlocks.MIXED_SLAB) {
			return;
		}

		if (!(this.minecraft.level.getBlockEntity(pos) instanceof MixedSlabBlockEntity mixedSlab)) {
			return;
		}

		Vec3 eye = this.minecraft.player.getEyePosition();
		Vec3 end = eye.add(this.minecraft.player.getViewVector(1.0F).scale(6.0));
		boolean topHalf = new AABB(pos).clip(eye, end)
				.map(hit -> hit.y > pos.getY() + 0.5)
				.orElse(this.minecraft.player.getEyeY() > pos.getY() + 0.5);
		BlockState kept = topHalf ? mixedSlab.getBottom() : mixedSlab.getTop();
		SlabType keepType = topHalf ? SlabType.BOTTOM : SlabType.TOP;

		this.urc$predictedRemainder = kept.setValue(SlabBlock.TYPE, keepType);
	}

	@Inject(method = "destroyBlock", at = @At("RETURN"))
	private void urc$restoreRemainder(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState remainder = this.urc$predictedRemainder;
		this.urc$predictedRemainder = null;

		if (remainder == null || this.minecraft.level == null) {
			return;
		}

		if (this.minecraft.level.getBlockState(pos).isAir()) {
			this.minecraft.level.setBlock(pos, remainder, 3);
		}
	}
}
