package com.aid;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.List;

public class DiamondHearts {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                checkHealing(player);
            }
        });
    }

    private static void checkHealing(Player player) {
        if (player.level().isClientSide()) return;

        // Проверяем, горит ли игрок
        if (!player.isOnFire() && !player.isInLava()) return;

        // Ищем алмазы рядом
        AABB area = player.getBoundingBox().inflate(3.0);
        List<ItemEntity> diamonds = player.level().getEntitiesOfClass(
                ItemEntity.class,
                area,
                item -> item.getItem().getItem() == Items.DIAMOND
        );

        if (diamonds.isEmpty()) return;

        // Берем первый алмаз
        ItemEntity diamond = diamonds.get(0);
        int count = diamond.getItem().getCount();

        // Лечим игрока (1 алмаз = 2 HP = 1 сердце)
        float healAmount = count * 2.0f;

        // Проверяем, нужно ли лечение
        if (player.getHealth() >= player.getMaxHealth()) {
            return; // У игрока полное здоровье
        }

        // Лечим (не больше максимума)
        float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);

        // Удаляем алмазы
        diamond.discard();

        // Сообщение
        player.displayClientMessage(
                Component.literal("§c🔥 Алмаз сгорел! §a❤ Восстановлено §c" + count + " §aсердец!"),
                true
        );

        // Эффекты
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Частицы
        for (int i = 0; i < 10; i++) {
            player.level().addParticle(
                    ParticleTypes.HEART,
                    player.getX() + (Math.random() - 0.5) * 2,
                    player.getY() + 1 + Math.random() * 2,
                    player.getZ() + (Math.random() - 0.5) * 2,
                    0, 0.1, 0
            );
        }
    }
}