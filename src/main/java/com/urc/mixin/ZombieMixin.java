package com.urc.mixin;

import com.urc.URCSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Villagers always turn into zombie villagers when killed by a zombie.
 *
 * <p>Vanilla gates this on difficulty inside {@code Zombie#killedEntity}: easy does nothing, normal
 * converts with a 50% chance and hard always converts. Reporting {@link Difficulty#HARD} for the
 * difficulty lookups in that method makes every kill convert, without touching any other zombie
 * behaviour.</p>
 */
@Mixin(Zombie.class)
public abstract class ZombieMixin {
	@Redirect(
			method = "killedEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;")
	)
	private Difficulty urc$alwaysInfect(ServerLevel level) {
		return URCSettings.villagerSafeDiscount ? Difficulty.HARD : level.getDifficulty();
	}
}
