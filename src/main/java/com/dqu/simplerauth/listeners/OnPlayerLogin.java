package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import net.minecraft.server.MinecraftServer;

public class OnPlayerLogin {
    public static boolean canUseOnlineAuth(MinecraftServer server, String username) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        boolean isGlobalAuth = ConfigManager.getAuthType().equals("global");
        if (!forcedOnlineAuth && !optionalOnlineAuth) {
            return false;
        }

        if (isGlobalAuth) return false;
        if (forcedOnlineAuth) return !ConfigManager.forcePlayerOffline(username) && AuthMod.doesMinecraftAccountExist(username);
        else return DbManager.isPlayerRegistered(username) && DbManager.getUseOnlineAuth(username);
    }
}
