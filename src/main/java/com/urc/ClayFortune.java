package com.urc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Fortune clay helper: a Fortune shovel always yields at least 5 clay balls from a clay block.
 *
 * <p>Vanilla clay has 4 clay balls and no fortune function in its loot table. This keeps the
 * vanilla drops (so datapack changes survive) and tops the clay balls up to
 * {@code 4 + fortune level}.</p>
 *
 * <p>It is a plain helper rather than its own mixin because {@link com.urc.mixin.BlockMixin}
 * already redirects the {@code Block.getDrops} call in {@code dropResources}. Two mixins cannot
 * redirect the same instruction.</p>
 */
public final class ClayFortune {
	private ClayFortune() {
	}

	public static List<ItemStack> apply(BlockState state, ServerLevel level, Entity breaker,
			ItemInstance tool, List<ItemStack> drops) {
		if (!URCSettings.fortuneClay || !state.is(Blocks.CLAY) || !(breaker instanceof Player player)) {
			return drops;
		}

		if (!(tool instanceof ItemStack toolStack) || !toolStack.is(ItemTags.SHOVELS)) {
			return drops;
		}

		Holder<Enchantment> fortune = level.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.FORTUNE);
		int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(fortune, player);

		if (fortuneLevel <= 0) {
			return drops;
		}

		int wanted = 4 + fortuneLevel;
		int have = drops.stream()
				.filter(stack -> stack.is(Items.CLAY_BALL))
				.mapToInt(ItemStack::getCount)
				.sum();

		if (have >= wanted) {
			return drops;
		}

		List<ItemStack> result = new ArrayList<>(drops);
		int missing = wanted - have;

		while (missing > 0) {
			int count = Math.min(missing, 64);
			result.add(new ItemStack(Items.CLAY_BALL, count));
			missing -= count;
		}

		return result;
	}
}
