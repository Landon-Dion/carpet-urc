package com.urc;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Items;
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
		URCBlockEntities.register();
		registerEnchantingEvents();
		CarpetServer.manageExtension(EXTENSION);

		// Fallback for the (unusual) case where Carpet's game-start hook already ran.
		EXTENSION.registerRules();

		LOGGER.info("URC {} loaded, Carpet extension registered", VERSION);
	}

	/**
	 * Lets hoppers pass the anvil's enchantment applicability check when {@code fastHopper} is on.
	 *
	 * <p>Fabric API already redirects that check in {@code AnvilMenu.createResult} to fire
	 * {@link EnchantmentEvents#ALLOW_ENCHANTING}. Redirecting the same instruction from URC made
	 * the two redirects mutually exclusive and crashed the game, so use the event instead.</p>
	 */
	private static void registerEnchantingEvents() {
		EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, stack, context) -> {
			if (URCSettings.fastHopper
					&& context == EnchantingContext.ACCEPTABLE
					&& stack.is(Items.HOPPER)) {
				return TriState.TRUE;
			}

			return TriState.DEFAULT;
		});
	}
}
