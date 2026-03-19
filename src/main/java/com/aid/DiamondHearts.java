package com.aid;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.List;
import java.util.HashMap;
import java.util.UUID;

public class DiamondHearts {

    // Карта для отслеживания выброшенных алмазов
    private static final HashMap<UUID, Long> diamondDropTimes = new HashMap<>();
    private static final int BURN_DELAY = 100; // 5 секунд (20 тиков = 1 сек)

    // Проверка, горит ли игрок
    public static boolean isPlayerOnFire(Player player) {
        return player.isOnFire() || player.isInLava();
    }

    // Подсчет алмазов вокруг игрока
    public static int countNearbyDiamonds(Player player) {
        Level level = player.level();
        AABB area = player.getBoundingBox().inflate(3.0); // Радиус 3 блока

        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                area,
                item -> item.getItem().getItem() == Items.DIAMOND
        );

        return items.size();
    }

    // Сжигание алмазов и восстановление сердец
    public static void burnDiamondsAndHeal(Player player) {
        Level level = player.level();
        if (level.isClientSide()) return;

        // Считаем алмазы рядом
        int diamondCount = countNearbyDiamonds(player);

        if (diamondCount == 0) return;

        // Удаляем алмазы
        AABB area = player.getBoundingBox().inflate(3.0);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area,
                item -> item.getItem().getItem() == Items.DIAMOND);

        for (ItemEntity item : items) {
            item.discard(); // Удаляем предмет
        }

        // Восстанавливаем сердца (каждый алмаз = 1 сердце = 2 HP)
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            float currentMax = player.getMaxHealth();
            float newMax = Math.min(40.0f, currentMax + diamondCount * 2);
            maxHealthAttr.setBaseValue(newMax);
            player.heal(diamondCount * 2);

            // Эффекты
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

            // Частицы
            for (int i = 0; i < 20; i++) {
                level.addParticle(
                        ParticleTypes.HEART,
                        player.getX() + (Math.random() - 0.5) * 2,
                        player.getY() + 1 + Math.random() * 2,
                        player.getZ() + (Math.random() - 0.5) * 2,
                        0, 0.1, 0
                );

                level.addParticle(
                        ParticleTypes.FLAME,
                        player.getX() + (Math.random() - 0.5) * 3,
                        player.getY() + Math.random() * 2,
                        player.getZ() + (Math.random() - 0.5) * 3,
                        0, 0.1, 0
                );
            }

            // Сообщение
            player.displayClientMessage(
                    Component.literal(String.format(
                            "§c🔥 %d алмазов сгорели! §a❤ Ты получил §c+%d сердец!",
                            diamondCount, diamondCount
                    )),
                    true
            );
        }
    }

    // Проверка на сжигание (вызывается каждый тик)
    public static void checkForBurning(Player player) {
        if (player.level().isClientSide()) return;

        // Проверяем, горит ли игрок
        if (isPlayerOnFire(player)) {
            // Проверяем, есть ли алмазы рядом
            if (countNearbyDiamonds(player) > 0) {
                // Добавляем в карту или обновляем время
                diamondDropTimes.put(player.getUUID(), System.currentTimeMillis());
            }
        } else {
            // Если игрок не горит, удаляем из карты
            diamondDropTimes.remove(player.getUUID());
        }

        // Проверяем, прошло ли достаточно времени
        if (diamondDropTimes.containsKey(player.getUUID())) {
            long dropTime = diamondDropTimes.get(player.getUUID());
            if (System.currentTimeMillis() - dropTime > BURN_DELAY * 50) { // 50ms per tick
                burnDiamondsAndHeal(player);
                diamondDropTimes.remove(player.getUUID());
            }
        }
    }
}