package com.urc;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;

/**
 * Carpet rules contributed by URC.
 *
 * <p>The rule name is the field name, so it is toggled with
 * {@code /carpet <ruleName> true}. Descriptions live in the lang files.</p>
 */
public final class URCSettings {
	/**
	 * Removes the vanilla attack cooldown entirely: every hit is a full-charge hit
	 * (full damage, crit eligibility, sweep, knockback) and the minimum attack charge
	 * gate is bypassed. While enabled, armor stands also stop taking damage from players.
	 */
	@Rule(categories = { "urc" })
	public static boolean noAttackCooldown = false;

	/**
	 * Removes the per-target hurt immunity window from player damage, so every attack packet
	 * the server processes deals damage - including several within the same game tick.
	 */
	@Rule(categories = { "urc" })
	public static boolean noAttackSpeedLimit = false;

	/**
	 * Accepts any closed obsidian loop in a vertical plane as a nether portal frame, instead of
	 * only vanilla rectangles, as long as the enclosed area fits the vanilla size limits.
	 */
	@Rule(categories = { "urc" })
	public static boolean customNetherPortal = false;

	/**
	 * Additionally accepts closed obsidian loops lying flat on the ground (horizontal plane) as
	 * portal frames and fills them with ground-facing portal blocks.
	 */
	@Rule(categories = { "urc" })
	public static boolean horizontalNetherPortal = false;

	/**
	 * Radius (in blocks) used when searching for an existing portal in the overworld side of a
	 * nether trip. Vanilla hardcodes 128; the nether side keeps its vanilla 16.
	 */
	@Rule(categories = { "urc" }, validators = { PortalSearchRadius.class })
	public static int netherPortalSearchRadius = 128;


	/** Lets the anvil merge enchantments up to level 10 (vanilla max level still gates tables/loot). */
	@Rule(categories = { "urc" })
	public static boolean higherEnchantmentLevels = false;

	/** Removes the "Too Expensive" limit and the compounding prior-work penalty from anvils. */
	@Rule(categories = { "urc" })
	public static boolean noAnvilPenalty = false;

	/** One cure (or Hero of the Village) gives the maximum trade discount. */
	@Rule(categories = { "urc" })
	public static boolean villagerFastDiscount = false;

	/** Villagers always convert to zombie villagers when killed by a zombie, on any difficulty. */
	@Rule(categories = { "urc" })
	public static boolean villagerSafeDiscount = false;


	/** Efficient iron golem farms: 5 tick golem detection, single-villager panic, 1 tick gossip. */
	@Rule(categories = { "urc" })
	public static boolean efficientIronFarm = false;


	/** Looting also applies to iron golem drops (vanilla's golem loot table has no looting). */
	@Rule(categories = { "urc" })
	public static boolean whiteLooting = false;


	/** Every furnace/smoker/blast furnace recipe takes 1 tick. */
	@Rule(categories = { "urc" })
	public static boolean superFastFurnace = false;

	/** A Fortune shovel always yields at least 5 clay balls from clay. */
	@Rule(categories = { "urc" })
	public static boolean fortuneClay = false;

	/** Keeps the search radius sane: huge values force-load an enormous amount of chunks. */
	public static class PortalSearchRadius extends Validator<Integer> {
		@Override
		public Integer validate(CommandSourceStack source, CarpetRule<Integer> rule, Integer value, String userInput) {
			return value != null && value >= 16 && value <= 512 ? value : null;
		}

		@Override
		public String description() {
			return "16..512 blocks";
		}
	}

	private URCSettings() {
	}
}
