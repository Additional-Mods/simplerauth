package com.dqu.simplerauth.listeners;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class OnPlayerConnect {
    public static void listen(ServerPlayerEntity player) {
        player.setInvulnerable(true);
        player.sendMessage(new TranslatableText("player.connect.authenticate"), false);
    }
}
