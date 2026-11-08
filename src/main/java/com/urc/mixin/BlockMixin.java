package com.urc.mixin;

import com.urc.ClayFortune;
import com.urc.URCSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Silk Touch drops of ice, packed ice and blue ice still receive the Fortune multiplier.
 *
 * <p>With both enchantments on the tool the loot table picks the silk-touch pool, so Fortune never
 * runs. This multiplies the silk-touch drop (a stack of the block itself) by {@code level + 1}.
 * The rule is deliberately limited to the three ice blocks; it does not duplicate ores or other
 * silk-touch drops.</p>
 */
@Mixin(Block.class)
public abstract class BlockMixin {
	@Redirect(
			method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;"
			)
	)
	private static List<ItemStack> urc$silkTouchFortune(BlockState state, ServerLevel level, BlockPos pos,
			BlockEntity blockEntity, Entity breaker, ItemInstance tool) {
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, breaker, tool);
		drops = ClayFortune.apply(state, level, breaker, tool, drops);

		if (!URCSettings.silkTouchFortune
				|| !(breaker instanceof Player)
				|| !(tool instanceof ItemStack stack)
				|| !(state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE))) {
			return drops;
		}

		Holder<Enchantment> silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
		Holder<Enchantment> fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
		ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

		if (enchantments.getLevel(silkTouch) <= 0 || enchantments.getLevel(fortune) <= 0) {
			return drops;
		}

		int fortuneLevel = enchantments.getLevel(fortune);
		List<ItemStack> result = new ArrayList<>(drops.size());

		for (ItemStack drop : drops) {
			// Only the "block itself" drop comes from the silk-touch pool.
			if (drop.is(state.getBlock().asItem())) {
				// Vanilla ore_drops formula: 1 + max(0, random(level + 2) - 1).
				int bonus = level.getRandom().nextInt(fortuneLevel + 2) - 1;
				int total = drop.getCount() * (Math.max(bonus, 0) + 1);

				while (total > 0) {
					int count = Math.min(total, drop.getMaxStackSize());
					result.add(new ItemStack(drop.getItem(), count));
					total -= count;
				}
			} else {
				result.add(drop);
			}
		}

		return result;
	}
}
