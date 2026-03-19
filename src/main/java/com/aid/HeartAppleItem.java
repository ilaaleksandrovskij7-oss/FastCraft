package com.aid;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

public class HeartAppleItem extends Item {

    Random random = new Random();


    public HeartAppleItem() {
        super(new Item.Properties()
                .stacksTo(5)              // Можно стакать по 64
                .rarity(Rarity.EPIC)       // Фиолетовое название (как у яблока Нотча)
                .food(new FoodProperties.Builder()
                        .alwaysEat()            // Можно есть всегда (даже с полным голодом)
                        .fast()                 // Быстрое поедание
                        .build()
                )
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Вызываем родительский метод (он уменьшит еду и т.д.)
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {
            // Увеличиваем максимальное здоровье на 2 HP (1 сердце)
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                float newMaxHealth = Math.min(40.0f, player.getMaxHealth() + 2.0f);
                maxHealthAttr.setBaseValue(newMaxHealth);
                player.heal(2.0f); // Лечим на то же количество

                // Эффекты
                player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);

                // Сообщение
                player.displayClientMessage(
                        Component.literal(String.format(
                                "§d❤ Сердечное яблоко! Теперь у тебя §c%d сердец",
                                (int)(newMaxHealth / 2)
                        )),
                        true
                );

                // Частицы
                for (int i = 0; i < 10; i++) {
                    level.addParticle(
                            ParticleTypes.HEART,
                            player.getX() + (random.nextDouble() - 0.5) * 2,
                            player.getY() + 1 + random.nextDouble() * 2,
                            player.getZ() + (random.nextDouble() - 0.5) * 2,
                            0, 0.1, 0
                    );
                }
            }
        }

        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Эффект чар (как у яблока Нотча)
    }
}