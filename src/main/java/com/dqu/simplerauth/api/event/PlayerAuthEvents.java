package com.dqu.simplerauth.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class PlayerAuthEvents {
    /**
     * Called when a player logs in.
     */
    public static final Event<PlayerLogin> PLAYER_LOGIN = EventFactory.createArrayBacked(PlayerLogin.class, callbacks -> (player, via) -> {
        for (PlayerLogin callback : callbacks) {
            callback.onPlayerLogin(player, via);
        }
    });

    /**
     * Called when a player fails the password.
     */
    public static final Event<PlayerLoginFail> PLAYER_LOGIN_FAIL = EventFactory.createArrayBacked(PlayerLoginFail.class, callbacks -> (player, fails) -> {
        for (PlayerLoginFail callback : callbacks) {
            callback.onPlayerLoginFail(player, fails);
        }
    });

    /**
     * Called when a player registers.
     */
    public static final Event<PlayerRegister> PLAYER_REGISTER = EventFactory.createArrayBacked(PlayerRegister.class, callbacks -> player -> {
        for (PlayerRegister callback : callbacks) {
            callback.onPlayerRegister(player);
        }
    });

    /**
     * Called when account information of a player has been modified
     */
    public static final Event<PlayerAccountModified> PLAYER_ACCOUNT_MODIFIED = EventFactory.createArrayBacked(PlayerAccountModified.class, callbacks -> (player, parameter, value) -> {
       for (PlayerAccountModified callback : callbacks) {
           callback.onPlayerAccountModified(player, parameter, value);
       }
    });

    @FunctionalInterface
    public interface PlayerLogin {
        /**
         * Called when a player logs in.
         * 
         * @param player the player
         * @param via loginCommand, onlineAuth, session, permissionLevel
         */
        void onPlayerLogin(ServerPlayerEntity player, String via);
    }

    @FunctionalInterface
    public interface PlayerLoginFail {
        void onPlayerLoginFail(ServerPlayerEntity player, int fails);
    }

    @FunctionalInterface
    public interface PlayerRegister {
        void onPlayerRegister(ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface PlayerAccountModified {
        /**
         * Called when account information of a player has been modified
         *
         * @param player Player
         * @param parameter unregister, onlineAuth, 2fa, password
         * @param value New value of the given parameter
         */
        void onPlayerAccountModified(ServerPlayerEntity player, String parameter, @Nullable String value);
    }

}
