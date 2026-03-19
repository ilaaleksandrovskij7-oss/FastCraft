package com.aid;

import com.aid.HeartAppleItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

    // Наше сердечное яблоко
    public static final Item HEART_APPLE = new HeartAppleItem();

    public static void register() {
        // Регистрируем с ID "yourmod:heart_apple"
        Registry.register(
                BuiltInRegistries.ITEM,
                new ResourceLocation("fastcraft", "heart_apple"),
                HEART_APPLE
        );
    }
}