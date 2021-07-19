package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerMove {
    public static boolean canMove(ServerPlayerEntity player) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        boolean authenticated = playerObject.isAuthenticated();
        if (!authenticated) {
            player.teleport(player.getX(), player.getY(), player.getZ());
        }
        return authenticated;
    }
}
