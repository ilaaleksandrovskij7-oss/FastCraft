package com.aid;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class notDie {

    public static boolean dieC(LivingEntity livingEntity, DamageSource damageSource, float damageAmount) {

        if (livingEntity instanceof Player) {
            Player plr = (Player) livingEntity;

            if (plr.getMaxHealth() <= 2){
                if (plr instanceof ServerPlayer){
                    ServerPlayer splr = (ServerPlayer) plr;
                    plr.setHealth(1);
                    splr.setGameMode(GameType.SPECTATOR);
                    plr.displayClientMessage(Component.literal("§4 you dead!"), true);
                }
            } else {
                float hp = plr.getMaxHealth();
                AttributeInstance Mhp = plr.getAttribute(Attributes.MAX_HEALTH);
                Mhp.setBaseValue(hp - 2);
                plr.setHealth(hp - 2);
            }

            return false;

        }
        return true;
    }


}