package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.listeners.OnPlayerConnect;
import com.dqu.simplerauth.listeners.OnPlayerLeave;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.LangManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        OnPlayerConnect.listen(player);
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void remove(ServerPlayerEntity player, CallbackInfo ci) {
        OnPlayerLeave.listen(player);
    }

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    public void canPlayerJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        String username = profile.getName();
        try {
            Pattern regex = Pattern.compile(ConfigManager.getString("username-regex"));
            Matcher matcher = regex.matcher(username);
            if (!matcher.matches()) {
                cir.setReturnValue(LangManager.getLiteralText("player.invalid_username"));
            }
        } catch (Exception e) {
            AuthMod.LOGGER.error(LangManager.get("config.incorrect"));
            AuthMod.LOGGER.warn("Skipping regex username verification as the config is incorrect.");
        }

        if (ConfigManager.getBoolean("prevent-logging-another-location")) {
            for (ServerPlayerEntity player : players) {
                if (player.getEntityName().equals(username)) {
                    cir.setReturnValue(LangManager.getLiteralText("player.connect.alreadylogged"));
                }
            }
        }
    }
}
