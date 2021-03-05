package me.londiuh.login;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerLogin {
    private ServerPlayerEntity player;
    private boolean loggedIn;

    public PlayerLogin(ServerPlayerEntity player) {
        this.player = player;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
