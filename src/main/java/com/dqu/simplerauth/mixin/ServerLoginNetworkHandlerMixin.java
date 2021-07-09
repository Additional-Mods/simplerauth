package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow @Nullable GameProfile profile;
    private static final Gson GSON = new Gson();

    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean doAuthenticatePlayerWithMojang(MinecraftServer server) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!server.isOnlineMode() || (!forcedOnlineAuth && !optionalOnlineAuth)) {
            return false;
        }

        //noinspection ConstantConditions - this.profile was set just before
        String username = this.profile.getName();
        if (forcedOnlineAuth) {
            return doesMinecraftAccountExist(username);
        } else return DbManager.isPlayerRegistered(username) && DbManager.getUseOnlineAuth(username);
    }

    private static boolean doesMinecraftAccountExist(String username) {
        String content = "";
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();
                String out;
                while ((out = bufferedReader.readLine()) != null) {
                    stringBuilder.append(out);
                }
                content = stringBuilder.toString();
            } else return false;
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
            return false;
        }

        JsonObject jsonObject = GSON.fromJson(content, JsonObject.class);
        return jsonObject.has("id");
    }
}
