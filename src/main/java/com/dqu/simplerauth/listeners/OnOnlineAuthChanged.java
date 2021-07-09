package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.CacheManager;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.UUID;

public class OnOnlineAuthChanged {
    public static void onEnabled(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return; // Shouldn't happen
        PlayerManager playerManager = server.getPlayerManager();

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();
            JsonObject cachedAccount = CacheManager.getMinecraftAccount(player.getEntityName());
            if (cachedAccount == null) {
                return; // Shouldn't happen
            }

            String onlineUuid = cachedAccount.get("online-uuid").getAsString();
            GameProfile onlineProfile = new GameProfile(UUID.fromString(onlineUuid), player.getEntityName());
            GameProfile offlineProfile = player.getGameProfile(); // The player is offline when this is executed
            if (whitelist.isAllowed(offlineProfile)) {
                whitelist.remove(offlineProfile);
                whitelist.add(new WhitelistEntry(onlineProfile));
                try {
                    whitelist.save();
                } catch (IOException e) {
                    AuthMod.LOGGER.error("Failed to save updated whitelist for username '{}', who enabled online authentication", player.getEntityName(), e);
                }
            }
        }
    }

    public static void onDisabled(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return; // Shouldn't happen
        PlayerManager playerManager = server.getPlayerManager();

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();
            JsonObject cachedAccount = CacheManager.getMinecraftAccount(player.getEntityName());
            if (cachedAccount == null) {
                return; // Shouldn't happen
            }

            String offlineUuid = cachedAccount.get("offline-uuid").getAsString();
            GameProfile offlineProfile = new GameProfile(UUID.fromString(offlineUuid), player.getEntityName());
            GameProfile onlineProfile = player.getGameProfile(); // The player is online when this is executed
            if (whitelist.isAllowed(onlineProfile)) {
                whitelist.remove(onlineProfile);
                whitelist.add(new WhitelistEntry(offlineProfile));
                try {
                    whitelist.save();
                } catch (IOException e) {
                    AuthMod.LOGGER.error("Failed to save updated whitelist for username '{}', who disabled online authentication", player.getEntityName(), e);
                }
            }
        }
    }
}
