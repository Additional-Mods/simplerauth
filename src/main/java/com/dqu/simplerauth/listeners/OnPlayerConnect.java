package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.managers.LangManager;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerConnect {
    public static void listen(ServerPlayerEntity player) {
        player.setInvulnerable(true);
        player.stopRiding();
        player.sendMessage(LangManager.getLiteralText("player.connect.authenticate"), false);
    }
}
