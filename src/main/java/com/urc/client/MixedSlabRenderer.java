package com.urc.client;

import com.urc.URC;
import com.urc.block.MixedSlabBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the two halves of a mixed slab by submitting their block models at the block position
 * (the lower half uses {@code type=bottom}, the upper half {@code type=top}).
 */
public class MixedSlabRenderer implements BlockEntityRenderer<MixedSlabBlockEntity, MixedSlabRenderState> {
	private static boolean LOGGED;
	@Override
	public MixedSlabRenderState createRenderState() {
		return new MixedSlabRenderState();
	}

	@Override
	public void extractRenderState(MixedSlabBlockEntity blockEntity, MixedSlabRenderState state, float partialTicks,
			Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

		if (!LOGGED) {
			LOGGED = true;
			URC.LOGGER.info("[URC] mixed slab extract: pos={} bottom={} top={}", blockEntity.getBlockPos(), blockEntity.getBottom(), blockEntity.getTop());
		}

		state.bottom = null;
		state.top = null;

		if (blockEntity.getLevel() instanceof ClientLevel level) {
			BlockPos pos = blockEntity.getBlockPos();
			Holder<Biome> biome = level.getBiome(pos);
			BlockState bottom = blockEntity.getBottom();
			BlockState top = blockEntity.getTop();

			// Old worlds saved both halves as type=bottom; fix that up at render time.
			if (bottom.hasProperty(SlabBlock.TYPE)) {
				bottom = bottom.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
			}

			if (top.hasProperty(SlabBlock.TYPE)) {
				top = top.setValue(SlabBlock.TYPE, SlabType.TOP);
			}

			if (!bottom.isAir()) {
				state.bottom = createMovingBlock(pos, bottom, biome, level);
			}

			if (!top.isAir()) {
				state.top = createMovingBlock(pos, top, biome, level);
			}
		}
	}

	@Override
	public void submit(MixedSlabRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.bottom != null) {
			SubmitCompat.submitMovingBlock(collector, poseStack, state.bottom);
		}

		if (state.top != null) {
			SubmitCompat.submitMovingBlock(collector, poseStack, state.top);
		}
	}

	private static MovingBlockRenderState createMovingBlock(BlockPos pos, BlockState blockState, Holder<Biome> biome, ClientLevel level) {
		MovingBlockRenderState movingBlock = new MovingBlockRenderState();
		movingBlock.randomSeedPos = pos;
		movingBlock.blockPos = pos;
		movingBlock.blockState = blockState;
		movingBlock.biome = biome;
		movingBlock.cardinalLighting = level.cardinalLighting();
		movingBlock.lightEngine = level.getLightEngine();
		return movingBlock;
	}
}
