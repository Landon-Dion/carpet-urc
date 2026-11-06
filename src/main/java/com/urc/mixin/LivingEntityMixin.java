package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hurt-immunity removal (noAttackSpeedLimit) and Looting on iron golems (whiteLooting).
 *
 * <p>Vanilla caps the effective hit rate on a single target with the 10 tick invulnerability
 * window in {@code LivingEntity#hurtServer}: once {@code invulnerableTime > 10}, an incoming
 * hit whose damage does not exceed {@code lastHurt} is discarded (returns false). Clearing the
 * counter before the hit is evaluated means every attack packet the server processes - including
 * several in the same game tick - deals its full damage, so the attack rate has no upper bound.</p>
 *
 * <p>Iron golems are the only mob whose loot table has no looting function at all, so the
 * enchantment does nothing on them; this adds one extra iron ingot per Looting level plus a
 * random number of poppies.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"))
	private void urc$clearHurtImmunityForPlayers(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Boolean> cir) {
		if (!URCSettings.noAttackSpeedLimit) {
			return;
		}

		Entity attacker = source.getEntity();

		if (attacker instanceof Player) {
			// invulnerableTime lives on Entity; the accessor keeps this working when the field
			// changes visibility between 26.x versions.
			((EntityAccessor) (Object) this).urc$setInvulnerableTime(0);
		}
	}

	@Inject(
			method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V",
			at = @At("TAIL")
	)
	private void urc$lootingOnIronGolem(ServerLevel level, DamageSource source, boolean hitByPlayer, CallbackInfo ci) {
		if (!URCSettings.whiteLooting || !((Object) this instanceof IronGolem golem)) {
			return;
		}

		if (!(source.getEntity() instanceof LivingEntity killer)) {
			return;
		}

		Holder<Enchantment> looting = level.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.LOOTING);
		int level_ = EnchantmentHelper.getEnchantmentLevel(looting, killer);

		if (level_ <= 0) {
			return;
		}

		golem.spawnAtLocation(level, new ItemStack(Items.IRON_INGOT, level_));

		int poppies = level.getRandom().nextInt(level_ + 1);

		if (poppies > 0) {
			golem.spawnAtLocation(level, new ItemStack(Items.POPPY, poppies));
		}
	}
}
