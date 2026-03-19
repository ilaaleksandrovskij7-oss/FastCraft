package com.aid;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;


import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import java.util.*;

public class FastCraft implements ModInitializer {


	@Override
	public void onInitialize(){
		fastools.register();
		ServerLivingEntityEvents.ALLOW_DEATH.register(notDie::dieC);
        ModItems.register();

        ServerTickEvents.START_SERVER_TICK.register(minecraftServer -> {
           for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()){
               compas.compasw(player);
           }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Проверяем, что это LivingEntity и не на клиенте
            if (!world.isClientSide() && entity instanceof LivingEntity livingTarget) {
                return HeartEater.onEntityInteract(player, livingTarget, hand);
            }
            return InteractionResult.PASS;
        });

        levitate.register();

        DiamondHearts.register();

		System.out.println("mod was started");
	}
}