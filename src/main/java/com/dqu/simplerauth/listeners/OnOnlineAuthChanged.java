package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.CacheManager;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.UUID;

public class OnOnlineAuthChanged {
    public static void onEnabled(ServerPlayerEntity player) {
        String username = player.getEntityName();
        MinecraftServer server = player.getServer();
        if (server == null) return; // Shouldn't happen
        PlayerManager playerManager = server.getPlayerManager();

        // Update uuids
        JsonObject cachedAccount = CacheManager.getMinecraftAccount(username);
        if (cachedAccount == null) {
            return; // Shouldn't happen
        }

        String onlineUuid = cachedAccount.get("online-uuid").getAsString();
        // UUID.fromString doesn't work on a uuid without dashes, we need to add them back before creating the UUID
        onlineUuid = onlineUuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        GameProfile onlineProfile = new GameProfile(UUID.fromString(onlineUuid), username);
        GameProfile offlineProfile = player.getGameProfile(); // The player is offline when this is executed

        // Update ops uuid
        OperatorList ops = playerManager.getOpList();
        OperatorEntry opEntry = ops.get(offlineProfile);
        if (opEntry != null) {
            ops.remove(offlineProfile);
            ops.add(new OperatorEntry(onlineProfile, opEntry.getPermissionLevel(), opEntry.canBypassPlayerLimit()));
            try {
                ops.save();
            } catch (IOException e) {
                AuthMod.LOGGER.error("Failed to save updated operator list for username '{}', who enabled online authentication", username, e);
            }
        }

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();

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

        // Update uuids
        UUID offlineUuid = PlayerEntity.getOfflinePlayerUuid(username);
        GameProfile offlineProfile = new GameProfile(offlineUuid, username);
        GameProfile onlineProfile = player.getGameProfile(); // The player is online when this is executed

        // Update ops uuid
        OperatorList ops = playerManager.getOpList();
        OperatorEntry opEntry = ops.get(onlineProfile);
        if (opEntry != null) {
            ops.remove(onlineProfile);
            ops.add(new OperatorEntry(offlineProfile, opEntry.getPermissionLevel(), opEntry.canBypassPlayerLimit()));
            try {
                ops.save();
            } catch (IOException e) {
                AuthMod.LOGGER.error("Failed to save updated operator list for username '{}', who disabled online authentication", username, e);
            }
        }

        if (playerManager.isWhitelistEnabled()) {
            // Update whitelist uuid
            Whitelist whitelist = playerManager.getWhitelist();

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
