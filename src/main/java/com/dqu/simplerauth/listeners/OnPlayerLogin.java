package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import net.minecraft.server.MinecraftServer;

public class OnPlayerLogin {
    public static boolean canUseOnlineAuth(MinecraftServer server, String username) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!server.isOnlineMode() || (!forcedOnlineAuth && !optionalOnlineAuth)) {
            return false;
        }

        if (forcedOnlineAuth) return AuthMod.doesMinecraftAccountExist(username);
        else return DbManager.isPlayerRegistered(username) && DbManager.getUseOnlineAuth(username);
    }
}
