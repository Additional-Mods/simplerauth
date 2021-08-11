package com.dqu.simplerauth;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerObject {
    private ServerPlayerEntity player;
    private boolean authenticated;

    public PlayerObject(ServerPlayerEntity player) {
        this.player = player;
        this.authenticated = false;
    }

    public void authenticate() {
        this.player.setInvulnerable(false);
        this.authenticated = true;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void updatePlayer(ServerPlayerEntity player) {
        if (this.player != player) {
            this.player = player;
        }
    }

    public void destroy() {
        this.authenticated = false;
    }
}
