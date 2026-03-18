package com.aid;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.chat.Component;
import java.util.HashMap;
import java.util.UUID;

public class notDie {
    private static final HashMap<UUID, Boolean> playerVeinMinerStatus = new HashMap<>();

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, damageAmount) -> {
            return dieC(livingEntity, damageSource, damageAmount);
        });
    }

    public static boolean getVeinMinerStatus(UUID playerId) {
        return playerVeinMinerStatus.getOrDefault(playerId, true);
    }

    public static void setVeinMinerStatus(UUID playerId, boolean status) {
        playerVeinMinerStatus.put(playerId, status);
    }

    public static boolean dieC(LivingEntity livingEntity, DamageSource damageSource, float damageAmount) {
        if (!(livingEntity instanceof Player)) {
            return true;
        }

        Player player = (Player) livingEntity;
        UUID playerId = player.getUUID();
        boolean isVeinMinerEnabled = getVeinMinerStatus(playerId);

        if (!isVeinMinerEnabled) {
            return true;
        }

        float healthAfterDamage = player.getHealth() - damageAmount;

        if (healthAfterDamage <= 0) {
            player.displayClientMessage(Component.literal("§4❤ Смерть отменена!"), true);

            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);

            if (maxHealthAttr != null) {
                double currentMax = maxHealthAttr.getBaseValue();
                double newMax = Math.max(2.0, currentMax - 2.0);
                maxHealthAttr.setBaseValue(newMax);
            }

            player.setHealth(player.getMaxHealth());

            int heartsLeft = (int)(player.getMaxHealth() / 2);
            player.displayClientMessage(Component.literal("§cОсталось сердечек: " + heartsLeft), true);

            return false;
        }

        return true;
    }
}