package com.dqu.simplerauth;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerObject {
    private final ServerPlayerEntity player;
    private boolean authenticated;

    public PlayerObject(ServerPlayerEntity player) {
        this.player = player;
        this.authenticated = false;
    }

    public void authenticate() {
        this.authenticated = true;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void destroy() {
        this.authenticated = false;
    }
}
