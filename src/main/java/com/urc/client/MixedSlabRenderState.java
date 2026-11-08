package com.urc.client;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class MixedSlabRenderState extends BlockEntityRenderState {
	public MovingBlockRenderState bottom;
	public MovingBlockRenderState top;
}
