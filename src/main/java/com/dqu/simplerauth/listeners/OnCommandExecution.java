package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnCommandExecution {
    public static boolean canSendCommand(ServerPlayerEntity player, String command) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        if (command.startsWith("login ") || command.startsWith("register ")) return true;
        return playerObject.isAuthenticated();
    }
}
