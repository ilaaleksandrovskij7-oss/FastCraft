package com.aid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class compas {
    public static void compasw(Player player){
        player.displayClientMessage(Component.literal("§1 X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ() + " :3"), true);
    }
}
