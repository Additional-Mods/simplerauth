package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnGameMessage {
    public static boolean canSendMessage(ServerPlayerEntity player, String message) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        if (message.startsWith("/login") || message.startsWith("/register")) return true;
        return playerObject.isAuthenticated();
    }
}
