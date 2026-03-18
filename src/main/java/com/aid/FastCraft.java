package com.aid;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FastCraft implements ModInitializer {


	@Override
	public void onInitialize(){
		fastools.register();
		ServerLivingEntityEvents.ALLOW_DEATH.register(notDie::dieC);

        ServerTickEvents.START_SERVER_TICK.register(minecraftServer -> {
           for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()){
               compas.compasw(player);
           }
        });

		System.out.println("mod was started");
	}
}