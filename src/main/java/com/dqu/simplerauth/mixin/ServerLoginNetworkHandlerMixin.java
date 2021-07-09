package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow @Nullable GameProfile profile;

    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean doAuthenticatePlayerWithMojang(MinecraftServer server) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!server.isOnlineMode() || (!forcedOnlineAuth && !optionalOnlineAuth)) {
            return false;
        }

        //noinspection ConstantConditions - this.profile was set just before
        String username = this.profile.getName();
        if (forcedOnlineAuth) return AuthMod.doesMinecraftAccountExist(username);
        else return DbManager.isPlayerRegistered(username) && DbManager.getUseOnlineAuth(username);
    }
}
