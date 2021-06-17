package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.LangManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerConnect {
    public static void listen(ServerPlayerEntity player) {
        player.setInvulnerable(true);
        player.sendMessage(LangManager.getLiteralText("player.connect.authenticate"), false);
    }
}
