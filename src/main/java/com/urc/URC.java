package com.urc;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URC - a Carpet Mod extension.
 *
 * <p>Carpet does not scan a custom entrypoint for extensions: the only supported way to
 * register one is {@link CarpetServer#manageExtension}. This runs on both the client and
 * the dedicated server, and the extension is in place before Carpet's own game-start hook,
 * so Carpet calls {@link URCCarpetExtension#onGameStarted()} for us.</p>
 */
public class URC implements ModInitializer {
	public static final String MOD_ID = "urc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer(MOD_ID)
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("dev");

	private static final URCCarpetExtension EXTENSION = new URCCarpetExtension();

	@Override
	public void onInitialize() {
		URCBlocks.register();
		CarpetServer.manageExtension(EXTENSION);

		// Fallback for the (unusual) case where Carpet's game-start hook already ran.
		EXTENSION.registerRules();

		LOGGER.info("URC {} loaded, Carpet extension registered", VERSION);
	}
}
