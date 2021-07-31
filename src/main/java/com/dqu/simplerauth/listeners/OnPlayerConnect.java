package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import com.dqu.simplerauth.managers.CacheManager;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnPlayerConnect {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void listen(ServerPlayerEntity player) {

        if (!player.hasPermissionLevel(ConfigManager.getInt("require-auth-permission-level"))) {
            PlayerObject playerObject = AuthMod.playerManager.get(player);
            playerObject.authenticate(player);
            return;
        }

        player.setInvulnerable(true);
        player.stopRiding();
        player.sendMessage(LangManager.getLiteralText("player.connect.authenticate"), false);

        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        boolean isGlobalAuth = ConfigManager.getAuthType().equals("global");
        // Forced online authentication does not require registration
        if ((forcedOnlineAuth || (optionalOnlineAuth && DbManager.isPlayerRegistered(player.getEntityName()))) && testPlayerOnline(player) && !isGlobalAuth) {
            PlayerObject playerObject = AuthMod.playerManager.get(player);
            playerObject.authenticate(player);
            player.sendMessage(LangManager.getLiteralText("command.general.authenticated"), false);
            AuthMod.LOGGER.info(player.getEntityName() + " is using an online account, authenticated automatically.");
            return;
        }

        boolean sessionenabled = ConfigManager.getBoolean("sessions-enabled");
        if (sessionenabled) {
            if (DbManager.sessionVerify(player.getEntityName(), player.getIp())) {
                PlayerObject playerObject = AuthMod.playerManager.get(player);
                playerObject.authenticate(player);
                DbManager.sessionCreate(player.getEntityName(), player.getIp());
                player.sendMessage(LangManager.getLiteralText("command.general.authenticated"), false);
                return;
            } else {
                DbManager.sessionDestroy(player.getEntityName());
            }
        }

        boolean hideposition = ConfigManager.getBoolean("hide-position");
        if (hideposition) {
            if (player.getX() > -1 && player.getX() < 1 && player.getZ() > -1 && player.getZ() < 1 && player.getY() < 1)
                return;
            DbManager.savePosition(player.getEntityName(), player.getPos());
            player.requestTeleport(0, 0, 0);
        }
    }

    public static boolean testPlayerOnline(ServerPlayerEntity player) {
        String uuid = player.getGameProfile().getId().toString().toLowerCase().replace("-", "");

        // Check if UUID is valid
        Pattern pattern = Pattern.compile("^[a-z0-9]{32}$");
        Matcher matcher = pattern.matcher(uuid);
        if (!matcher.matches()) return false;

        String realUuid;

        JsonObject cachedAccount = CacheManager.getMinecraftAccount(player.getEntityName());
        if (cachedAccount == null) {
            String content = "";
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + player.getEntityName()).openConnection();
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
            realUuid = jsonObject.get("id").getAsString().toLowerCase();

            CacheManager.addMinecraftAccount(player.getEntityName(), realUuid);
        } else {
            realUuid = cachedAccount.get("online-uuid").getAsString();
        }
        return uuid.equals(realUuid);
    }
}
