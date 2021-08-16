package com.dqu.simplerauth.listeners;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.dqu.simplerauth.managers.CacheManager;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

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
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        playerObject.updatePlayer(player);

        if (!player.hasPermissionLevel(ConfigManager.getInt("require-auth-permission-level"))) {
            playerObject.authenticate();
            PlayerAuthEvents.PLAYER_LOGIN.invoker().onPlayerLogin(player, "permissionLevel");
            return;
        }

        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        boolean isGlobalAuth = ConfigManager.getAuthType().equals("global");
        // Forced online authentication does not require registration
        if ((forcedOnlineAuth || (optionalOnlineAuth && DbManager.isPlayerRegistered(player.getEntityName()))) && testPlayerOnline(player) && !isGlobalAuth) {
            if (DbManager.getTwoFactorEnabled(player.getEntityName())) {
                player.sendMessage(LangManager.getLiteralText("player.connect.authenticate.2fa"), false);
                return;
            }
            playerObject.authenticate();
            PlayerAuthEvents.PLAYER_LOGIN.invoker().onPlayerLogin(player, "onlineAuth");
            player.sendMessage(LangManager.getLiteralText("command.general.authenticated"), false);
            AuthMod.LOGGER.info(player.getEntityName() + " is using an online account, authenticated automatically.");
            return;
        }

        if (ConfigManager.getBoolean("sessions-enabled")) {
            if (DbManager.sessionVerify(player.getEntityName(), player.getIp())) {
                playerObject.authenticate();
                PlayerAuthEvents.PLAYER_LOGIN.invoker().onPlayerLogin(player, "session");
                DbManager.sessionCreate(player.getEntityName(), player.getIp());
                player.sendMessage(LangManager.getLiteralText("command.general.authenticated"), false);
                return;
            } else {
                DbManager.sessionDestroy(player.getEntityName());
            }
        }

        player.setInvulnerable(true);
        player.stopRiding();
        player.sendMessage(LangManager.getLiteralText("player.connect.authenticate"), false);

        if (ConfigManager.getBoolean("hide-position") && DbManager.isPlayerRegistered(player.getEntityName())) {
            if (player.getX() > -1 && player.getX() < 1 && player.getZ() > -1 && player.getZ() < 1 && player.getY() < 1)
                return;
            DbManager.savePosition(player.getEntityName(), player.getPos());
            player.requestTeleport(0, 0, 0);
        } else if (ConfigManager.getBoolean("portal-teleport")) { // Not needed when player is in the void
            if (player.getServerWorld().getBlockState(player.getBlockPos()).isOf(Blocks.NETHER_PORTAL)) {
                teleportPlayerAway(player);
            }
        }
    }

    public static void teleportPlayerAway(ServerPlayerEntity player) {
        // Modified ChorusFruitItem#finishUsing
        boolean teleported = false;
        ServerWorld world = player.getServerWorld();
        while (!teleported) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
            double y = MathHelper.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8), world.getBottomY(), world.getBottomY() + world.getLogicalHeight() - 1);
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;

            if (player.teleport(x, y, z, true)) {
                teleported = true;
            }
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
