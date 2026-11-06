package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Anvil tweaks: higher enchantment levels and no "Too Expensive" penalty.
 *
 * <p>Vanilla clamps the merged level to {@code enchantment.getMaxLevel()} inside
 * {@code createResult}, so that single call is redirected to allow up to 10. The enchanting table
 * and loot still use the real max level, which is what keeps 6..10 book levels anvil-only.</p>
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Shadow
	@Final
	private DataSlot cost;

	@Redirect(
			method = "createResult",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I")
	)
	private int urc$higherEnchantmentLevels(Enchantment enchantment) {
		int vanillaMax = enchantment.getMaxLevel();
		return URCSettings.higherEnchantmentLevels ? Math.max(vanillaMax, 10) : vanillaMax;
	}

	@Redirect(
			method = "createResult",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;calculateIncreasedRepairCost(I)I")
	)
	private static int urc$noRepairCostGrowth(int previousCost) {
		return URCSettings.noAnvilPenalty ? 0 : AnvilMenu.calculateIncreasedRepairCost(previousCost);
	}

	@Redirect(
			method = "createResult",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I")
	)
	private int urc$capTooExpensiveCheck(DataSlot slot) {
		int value = slot.get();

		if (URCSettings.noAnvilPenalty) {
			return Math.min(value, 39);
		}

		return value;
	}

	@Inject(method = "createResult", at = @At("TAIL"))
	private void urc$clampDisplayedCost(CallbackInfo ci) {
		if (URCSettings.noAnvilPenalty && this.cost.get() > 39) {
			this.cost.set(39);
		}
	}
}
