package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * Efficiency V hoppers transfer one item per tick and can work on all five slots in the same tick.
 *
 * <p>Vanilla {@code tryMoveItems} moves at most one item per call. With the enchantment the tick is
 * replaced by up to five consecutive vanilla transfer attempts, so every slot can push/pull during
 * a single game tick.</p>
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
	@Invoker("tryMoveItems")
	private static boolean urc$tryMoveItems(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, BooleanSupplier action) {
		throw new AssertionError();
	}

	@Invoker("suckInItems")
	private static boolean urc$suckInItems(Level level, Hopper hopper) {
		throw new AssertionError();
	}

	/**
	 * Vanilla resets the cooldown with {@code setCooldown(0)} before each attempt; turning that
	 * into 1 would leave the hopper permanently "on cooldown". Only the 8 tick success value is
	 * replaced.
	 */
	@ModifyVariable(method = "setCooldown", at = @At("HEAD"), argsOnly = true)
	private int urc$efficiencyHopperSpeed(int cooldown) {
		if (!URCSettings.fastHopper || cooldown <= 1) {
			return cooldown;
		}

		return urc$isFastHopper((HopperBlockEntity) (Object) this) ? 1 : cooldown;
	}

	@Inject(method = "pushItemsTick", at = @At("HEAD"), cancellable = true)
	private static void urc$multiSlotTransfer(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, CallbackInfo ci) {
		if (!URCSettings.fastHopper || level.isClientSide() || !urc$isFastHopper(hopper)) {
			return;
		}

		ci.cancel();

		HopperAccessor accessor = (HopperAccessor) hopper;
		accessor.urc$setCooldownTime(-1);

		int moved = 0;

		while (moved < 5) {
			accessor.urc$setCooldownTime(0);

			if (!urc$tryMoveItems(level, pos, state, hopper, () -> urc$suckInItems(level, hopper))) {
				break;
			}

			moved++;
		}

		accessor.urc$setCooldownTime(moved > 0 ? 1 : 0);
	}

	private static boolean urc$isFastHopper(HopperBlockEntity hopper) {
		if (hopper.getLevel() == null) {
			return false;
		}

		ItemEnchantments enchantments = hopper.collectComponents().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

		if (enchantments.isEmpty()) {
			return false;
		}

		Holder<Enchantment> efficiency = hopper.getLevel().registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.EFFICIENCY);

		return enchantments.getLevel(efficiency) >= 5;
	}
}
