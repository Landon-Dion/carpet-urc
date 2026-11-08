package com.urc.client;

import com.urc.URC;
import com.urc.URCBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class URCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		URC.LOGGER.info("[URC] client init: registering mixed slab renderer for {}", URCBlockEntities.MIXED_SLAB);
		BlockEntityRendererRegistry.register(URCBlockEntities.MIXED_SLAB, context -> new MixedSlabRenderer());
	}
}
