package com.aid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;


public class HeartEater {

    // Проверка, можно ли съесть сердце моба
    public static boolean canEatHeart(Player player, LivingEntity target) {
        // Нельзя съесть игрока
        if (target instanceof Player) {
            player.displayClientMessage(
                    Component.literal("§cНельзя съесть сердце игрока!"),
                    true
            );
            return false;
        }

        // Проверяем, есть ли свободное место для сердечка (макс 20 сердец = 40 HP)
        if (player.getMaxHealth() >= 40.0f) {
            player.displayClientMessage(
                    Component.literal("§cУ тебя уже максимум сердец!"),
                    true
            );
            return false;
        }

        return true;
    }

    // Основной метод поедания
    public static void eatHeart(Player player, LivingEntity target) {
        if (!canEatHeart(player, target)) return;

        // Увеличиваем максимальное здоровье на 1 сердце (2 HP)
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            float newMaxHealth = player.getMaxHealth() + 2.0f;
            maxHealthAttr.setBaseValue(newMaxHealth);

            // Лечим игрока на это же количество
            player.heal(2.0f);

            // Эффекты и звуки
            player.playSound(SoundEvents.GENERIC_EAT, 1.0f, 1.0f);
            player.playSound(SoundEvents.PLAYER_BURP, 1.0f, 1.0f);

            // Сообщение
            player.displayClientMessage(
                    Component.literal(String.format(
                            "§a❤ Ты съел сердце! Теперь у тебя §c%d сердец",
                            (int)(newMaxHealth / 2)
                    )),
                    true
            );

            // Удаляем моба
            target.discard();

            // Выбрасываем пустую руку (эффект поедания)
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // Метод для проверки по клику
    public static InteractionResult onEntityInteract(Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) return InteractionResult.PASS;

        // Проверяем, пустая ли рука
        if (player.getItemInHand(hand).isEmpty()) {
            eatHeart(player, target);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}