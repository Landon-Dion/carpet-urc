package com.urc;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpet.utils.Translations;

import java.util.Map;

/**
 * Carpet extension that contributes the URC rules and their translations.
 *
 * <p>The rule is registered into Carpet's own {@link SettingsManager}, so it is toggled with
 * {@code /carpet noAttackCooldown true} (and persisted in {@code carpet.conf}) exactly like a
 * built-in Carpet rule, instead of creating a separate {@code /urc} command.</p>
 */
public class URCCarpetExtension implements CarpetExtension {
	private boolean rulesRegistered;

	@Override
	public void onGameStarted() {
		// Carpet has already created and populated its settings manager at this point.
		registerRules();
	}

	@Override
	public void onServerLoaded(net.minecraft.server.MinecraftServer server) {
		registerRules();
	}

	@Override
	public String version() {
		return URC.VERSION;
	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		Map<String, String> translations =
				Translations.getTranslationFromResourcePath("assets/" + URC.MOD_ID + "/lang/" + lang + ".json");

		if (translations.isEmpty() && !"en_us".equals(lang)) {
			translations = Translations.getTranslationFromResourcePath("assets/" + URC.MOD_ID + "/lang/en_us.json");
		}

		return translations;
	}

	/**
	 * Idempotent: safe to call from every Carpet lifecycle hook, so the rule exists even if the
	 * extension is registered after Carpet's own game-start hook.
	 */
	public void registerRules() {
		SettingsManager carpetSettings = CarpetServer.settingsManager;

		if (rulesRegistered || carpetSettings == null) {
			return;
		}

		rulesRegistered = true;
		carpetSettings.parseSettingsClass(URCSettings.class);
		URC.LOGGER.info("Registered Carpet rule(s) from {}", URCSettings.class.getSimpleName());
	}
}
