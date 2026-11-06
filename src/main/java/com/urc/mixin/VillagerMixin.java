package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Villager trade discount and iron golem farm tweaks.
 *
 * <p>Discount: one cure (major-positive reputation 5) or Hero of the Village pushes every offer
 * to the lowest price the game allows.</p>
 *
 * <p>Iron farm: the gossip cooldown drops from 1200 to 1 tick and a golem only needs a single
 * villager to agree instead of five.</p>
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {
	@Inject(method = "updateSpecialPrices", at = @At("TAIL"))
	private void urc$maxDiscount(Player player, CallbackInfo ci) {
		if (!URCSettings.villagerFastDiscount) {
			return;
		}

		Villager villager = (Villager) (Object) this;

		boolean curedAtLeastOnce = villager.getPlayerReputation(player) >= 5;
		boolean hero = player.hasEffect(net.minecraft.world.effect.MobEffects.HERO_OF_THE_VILLAGE);

		if (!curedAtLeastOnce && !hero) {
			return;
		}

		for (MerchantOffer offer : villager.getOffers()) {
			int price = offer.getCostA().getCount();

			if (price > 1) {
				offer.addToSpecialPriceDiff(-(price - 1));
			}
		}
	}

	@ModifyConstant(method = "gossip", constant = @Constant(longValue = 1200L))
	private long urc$fastGossipCooldown(long original) {
		return URCSettings.efficientIronFarm ? 1L : original;
	}

	@ModifyArg(
			method = "gossip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/npc/villager/Villager;spawnGolemIfNeeded(Lnet/minecraft/server/level/ServerLevel;JI)V"
			),
			index = 2
	)
	private int urc$oneVillagerIsEnough(int villagersNeeded) {
		return URCSettings.efficientIronFarm ? 1 : villagersNeeded;
	}

	/**
	 * Vanilla only ever tries to spawn a golem from {@code gossip()}, which needs a second villager
	 * to gossip with - a lone villager never attempts a spawn no matter how low the "villagers
	 * needed" count is. Poll every 5 ticks so a single villager is enough.
	 *
	 * <p>The vanilla "must have slept in the last 24000 ticks" requirement is dropped as well,
	 * otherwise a lone villager without a bed still would never qualify.</p>
	 */
	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void urc$singleVillagerGolemSpawn(ServerLevel level, CallbackInfo ci) {
		if (!URCSettings.efficientIronFarm) {
			return;
		}

		Villager villager = (Villager) (Object) this;

		if (villager.tickCount % 5 == 0) {
			villager.spawnGolemIfNeeded(level, level.getGameTime(), 1);
		}
	}

	@Inject(method = "golemSpawnConditionsMet", at = @At("HEAD"), cancellable = true)
	private void urc$skipSleepRequirement(long gameTime, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
		if (URCSettings.efficientIronFarm) {
			cir.setReturnValue(true);
		}
	}
}
