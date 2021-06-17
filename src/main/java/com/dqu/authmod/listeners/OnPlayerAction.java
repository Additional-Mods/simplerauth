package com.dqu.authmod.listeners;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.PlayerObject;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerAction {
    public static boolean canInteract(ServerPlayNetworkHandler networkHandler) {
        ServerPlayerEntity player = networkHandler.player;
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        return playerObject.isAuthenticated();
    }
}
