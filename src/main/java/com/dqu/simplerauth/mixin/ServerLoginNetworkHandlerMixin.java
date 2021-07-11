package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.listeners.OnPlayerLogin;
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
    private boolean useOnlineAuth(MinecraftServer server) {
        //noinspection ConstantConditions - this.profile was set just before
        return OnPlayerLogin.canUseOnlineAuth(server, this.profile.getName());
    }
}
