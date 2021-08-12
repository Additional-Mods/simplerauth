package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerMove {
    private static long lastPacket = 0;

    public static boolean canMove(ServerPlayerEntity player) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        boolean authenticated = playerObject.isAuthenticated();
        if (!authenticated && System.nanoTime() >= (lastPacket + 5000000L)) {
            player.requestTeleport(player.getX(), player.getY(), player.getZ());
            lastPacket = System.nanoTime();
        }
        return authenticated;
    }
}
