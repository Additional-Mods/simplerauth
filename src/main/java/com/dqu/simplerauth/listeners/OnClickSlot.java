package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnClickSlot {
    public static boolean canClickSlot(ServerPlayerEntity player) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        return playerObject.isAuthenticated();
    }
}
