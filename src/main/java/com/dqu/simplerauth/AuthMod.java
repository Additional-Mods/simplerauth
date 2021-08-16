package com.dqu.simplerauth;

import com.dqu.simplerauth.commands.*;
import com.dqu.simplerauth.managers.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    public static PlayerManager playerManager = new PlayerManager();

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
        DbManager.loadDatabase();
        CacheManager.loadCache();
        LangManager.loadTranslations(ConfigManager.getString("language"));
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LoginCommand.registerCommand(dispatcher);
            RegisterCommand.registerCommand(dispatcher);
            ChangePasswordCommand.registerCommand(dispatcher);
            OnlineAuthCommand.registerCommand(dispatcher);
            SimplerAuthCommand.registerCommand(dispatcher);
            TwoFactorCommand.registerCommand(dispatcher);
        }));
    }

    public static boolean doesMinecraftAccountExist(String username) {
        String content = "";
        if (CacheManager.getMinecraftAccount(username) != null) {
            return true;
        }

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
        CacheManager.addMinecraftAccount(username, jsonObject.get("id").getAsString());
        return true;
    }
}
