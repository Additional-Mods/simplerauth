package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.google.common.collect.Lists;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(ServerConfigHandler.class)
public class ServerConfigHandlerMixin {
    // This one may not be necessary, it is only used in conversion of old server files
    @Inject(method = "lookupProfile", at = @At(value = "HEAD"), cancellable = true)
    private static void lookupProfile(MinecraftServer server, Collection<String> bannedPlayers, ProfileLookupCallback callback, CallbackInfo ci) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!forcedOnlineAuth && !optionalOnlineAuth) {
            return;
        }

        ci.cancel();
        List<String> onlineBannedPlayers = Lists.newArrayList();
        for (String player : bannedPlayers) {
            if (!StringHelper.isEmpty(player)) {
                boolean onlinePlayer = forcedOnlineAuth ? AuthMod.doesMinecraftAccountExist(player) : DbManager.getUseOnlineAuth(player);
                if (onlinePlayer) onlineBannedPlayers.add(player);
                else {
                    UUID uuid = PlayerEntity.getOfflinePlayerUuid(player);
                    GameProfile profile = new GameProfile(uuid, player);
                    callback.onProfileLookupSucceeded(profile);
                }
            }
        }

        server.getGameProfileRepo().findProfilesByNames(onlineBannedPlayers.toArray(new String[0]), Agent.MINECRAFT, callback);
    }

    @Redirect(method = "getPlayerUuidByName", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private static boolean useOnlineModeForPlayer(MinecraftServer server, MinecraftServer server1, String name) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!forcedOnlineAuth && !optionalOnlineAuth) {
            return false;
        }

        if (forcedOnlineAuth) return AuthMod.doesMinecraftAccountExist(name);
        else return DbManager.getUseOnlineAuth(name);
    }
}
