package com.urc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;

/** 26.1.x submitMovingBlock(PoseStack, MovingBlockRenderState). */
public final class SubmitCompat {
	private SubmitCompat() {
	}

	public static void submitMovingBlock(SubmitNodeCollector collector, PoseStack poseStack, MovingBlockRenderState state) {
		collector.submitMovingBlock(poseStack, state);
	}
}
