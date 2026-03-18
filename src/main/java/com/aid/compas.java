package com.aid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class compas {
    public static void compasw(Player player){
        player.displayClientMessage(Component.literal("X: " + player.getX() + " Y: " + player.getY() + " Z: " + player.getZ() + " :3"), true);
    }
}
