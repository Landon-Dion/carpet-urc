package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Fortune clay: a Fortune shovel always yields at least 5 clay balls from a clay block.
 *
 * <p>Vanilla clay has 4 clay balls and no fortune function in its loot table. The redirect keeps
 * the vanilla drops (so datapack changes survive) and tops the clay balls up to
 * {@code 4 + fortune level}.</p>
 */
@Mixin(Block.class)
public abstract class ClayFortuneMixin {
	@Redirect(
			method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;"
			)
	)
	private static List<ItemStack> urc$fortuneClay(BlockState state, ServerLevel level, BlockPos pos,
			BlockEntity blockEntity, Entity breaker, ItemInstance tool) {
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, breaker, tool);

		if (!URCSettings.fortuneClay || !state.is(Blocks.CLAY) || !(breaker instanceof Player player)) {
			return drops;
		}

		if (!(tool instanceof ItemStack toolStack) || !(toolStack.getItem() instanceof ShovelItem)) {
			return drops;
		}

		Holder<Enchantment> fortune = level.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.FORTUNE);
		int level_ = EnchantmentHelper.getEnchantmentLevel(fortune, player);

		if (level_ <= 0) {
			return drops;
		}

		int wanted = 4 + level_;
		int have = drops.stream().filter(stack -> stack.is(Items.CLAY_BALL)).mapToInt(ItemStack::getCount).sum();

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
