package com.dqu.authmod.listeners;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.PlayerObject;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerLeave {
    public static void listen(ServerPlayerEntity player) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        playerObject.destroy();
    }
}
