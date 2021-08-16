package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwoFactorManager {
    private static String getSecretKey(ServerPlayerEntity player) {
        String username = player.getEntityName().replaceAll("[^a-zA-Z0-9]", "");
        return username + player.getUuidAsString() + DbManager.getHash(username);
    }

    public static String pair(ServerPlayerEntity player) {
        String username = player.getEntityName();
        String serverName = player.getServer().getServerMotd() + " @ " + player.getServer().getName();
        serverName = serverName.replace(" ", "%20").replaceAll("[^a-zA-Z0-9%@]", "");
        String secretKey = getSecretKey(player);

        return String.format("https://www.authenticatorApi.com/pair.aspx?AppName=%s&AppInfo=%s&SecretCode=%s", serverName, username, secretKey);
    }

    public static boolean validate(ServerPlayerEntity player, String pin) {
        String secretKey = getSecretKey(player);
        String url = String.format("https://www.authenticatorapi.com/Validate.aspx?Pin=%s&SecretCode=%s", pin, secretKey);
        String content = "";

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
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
            } else return true;
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
            return true;
        }

        return !content.equals("False");
    }
}
