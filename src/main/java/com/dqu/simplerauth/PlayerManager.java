package com.dqu.simplerauth;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class PlayerManager extends HashMap<String, PlayerObject> {
    public PlayerObject get(ServerPlayerEntity player) {
        String username = player.getEntityName();
        if (containsKey(username)) return super.get(username);
        PlayerObject playerObj = new PlayerObject(player);
        put(username, playerObj);
        return playerObj;
    }
}
