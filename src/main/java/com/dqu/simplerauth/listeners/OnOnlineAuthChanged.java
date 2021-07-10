package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.CacheManager;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.UUID;

public class OnOnlineAuthChanged {
    public static void onEnabled(ServerPlayerEntity player) {
        String username = player.getEntityName();
        MinecraftServer server = player.getServer();
        if (server == null) return; // Shouldn't happen
        PlayerManager playerManager = server.getPlayerManager();

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();
            JsonObject cachedAccount = CacheManager.getMinecraftAccount(username);
            if (cachedAccount == null) {
                return; // Shouldn't happen
            }

            String onlineUuid = cachedAccount.get("online-uuid").getAsString();
            // UUID.fromString doesn't work on a uuid without dashes, we need to add them back before creating the UUID
            onlineUuid = onlineUuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

            GameProfile onlineProfile = new GameProfile(UUID.fromString(onlineUuid), username);
            GameProfile offlineProfile = player.getGameProfile(); // The player is offline when this is executed

            if (whitelist.isAllowed(offlineProfile)) {
                whitelist.remove(offlineProfile);
                whitelist.add(new WhitelistEntry(onlineProfile));
                try {
                    whitelist.save();
                } catch (IOException e) {
                    AuthMod.LOGGER.error("Failed to save updated whitelist for username '{}', who enabled online authentication", username, e);
                }
            }
        }
    }

    public static void onDisabled(ServerPlayerEntity player) {
        String username = player.getEntityName();
        MinecraftServer server = player.getServer();
        if (server == null) return; // Shouldn't happen
        PlayerManager playerManager = server.getPlayerManager();

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();
            UUID offlineUuid = PlayerEntity.getOfflinePlayerUuid(username);
            GameProfile offlineProfile = new GameProfile(offlineUuid, username);
            GameProfile onlineProfile = player.getGameProfile(); // The player is online when this is executed

            if (whitelist.isAllowed(onlineProfile)) {
                whitelist.remove(onlineProfile);
                whitelist.add(new WhitelistEntry(offlineProfile));
                try {
                    whitelist.save();
                } catch (IOException e) {
                    AuthMod.LOGGER.error("Failed to save updated whitelist for username '{}', who disabled online authentication", username, e);
                }
            }
        }
    }
}
