package com.dqu.authmod.listeners;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.PlayerObject;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnGameMessage {
    public static boolean canSendMessage(ServerPlayNetworkHandler networkHandler, ChatMessageC2SPacket packet) {
        ServerPlayerEntity player = networkHandler.player;
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        String message = packet.getChatMessage();
        if (message.startsWith("/login") || message.startsWith("/register")) return true;
        return playerObject.isAuthenticated();
    }
}
