package com.dqu.authmod.listeners;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.PlayerObject;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerMove {
    public static boolean canMove(ServerPlayNetworkHandler networkHandler) {
        ServerPlayerEntity player = networkHandler.player;
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        boolean authenticated = playerObject.isAuthenticated();
        if (!authenticated) {
            player.teleport(player.getX(), player.getY(), player.getZ());
        }
        return authenticated;
    }
}
