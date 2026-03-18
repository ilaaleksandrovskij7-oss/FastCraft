package com.aid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class FastCraftClient implements ClientModInitializer {

	private static KeyMapping veinMinerKey;
	private static boolean veinMinerEnabled = true;

	@Override
	public void onInitializeClient() {
		// Регистрируем кейбинд
		veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.fastcraft.toggle_veinminer",
				GLFW.GLFW_KEY_G,
				"key.category.fastcraft"
		));

		// Проверяем нажатие каждый тик
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (veinMinerKey.consumeClick()) {
				veinMinerEnabled = !veinMinerEnabled;

				if (client.player != null) {
					String status = veinMinerEnabled ? "§aВКЛ" : "§cВЫКЛ";
					client.player.displayClientMessage(
							Component.literal("§6God mode: " + status),
							true
					);
				}
			}
		});
	}

	public static boolean isVeinMinerEnabled() {
		return veinMinerEnabled;
	}
}