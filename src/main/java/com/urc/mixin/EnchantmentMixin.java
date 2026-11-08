package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Silk Touch and Fortune are no longer mutually exclusive. */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
	@Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
	private static void urc$allowSilkTouchFortune(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
		if (!URCSettings.silkTouchFortune) {
			return;
		}

		boolean silkTouch = first.is(Enchantments.SILK_TOUCH) || second.is(Enchantments.SILK_TOUCH);
		boolean fortune = first.is(Enchantments.FORTUNE) || second.is(Enchantments.FORTUNE);

		if (silkTouch && fortune) {
			cir.setReturnValue(true);
		}
	}
}
