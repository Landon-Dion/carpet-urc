package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every cooking recipe finishes in one tick.
 *
 * <p>The furnace uses {@code cookingTime()} both for the initial total cook time and for the
 * value it resets to after each smelt, so overriding the recipe itself covers every furnace,
 * smoker and blast furnace without touching their tick code.</p>
 */
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
	@Inject(method = "cookingTime", at = @At("HEAD"), cancellable = true)
	private void urc$oneTickSmelting(CallbackInfoReturnable<Integer> cir) {
		if (URCSettings.superFastFurnace) {
			cir.setReturnValue(1);
		}
	}
}
