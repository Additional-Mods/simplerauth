package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;
import com.google.common.io.Files;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class DbManager {
    public static final int VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PATH = FabricLoader.getInstance().getConfigDir().resolve("simplerauth-database.json").toString();
    private static final File DBFILE = new File(PATH);
    private static JsonObject db = new JsonObject();

    public static void loadDatabase() {
        if (!DBFILE.exists()) {
            db.addProperty("version", VERSION);
            db.add("users", new JsonArray());
            saveDatabase();
        }

        try {
            BufferedReader bufferedReader = Files.newReader(DBFILE, StandardCharsets.UTF_8);
            db = GSON.fromJson(bufferedReader, JsonObject.class);
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
        }

        if (db.get("version") == null) convertDatabase(-1);
    }

    private static void saveDatabase() {
        try {
            BufferedWriter bufferedWriter = Files.newWriter(DBFILE, StandardCharsets.UTF_8);
            String json = GSON.toJson(db);
            bufferedWriter.write(json);
            bufferedWriter.close();
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
        }
    }

    private static JsonObject getPlayer(String username) {
        if (!DBFILE.exists()) return null;
        JsonArray users = db.get("users").getAsJsonArray();
        if (users.size() == 0) return null;
        for (int i = 0; i < users.size(); i++) {
            JsonObject user = users.get(i).getAsJsonObject();
            if (user.get("user").getAsString().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static boolean isPlayerRegistered(String username) {
        return getPlayer(username) != null;
    }

    public static void addPlayerDatabase(String username, String password) {
        JsonArray users = db.get("users").getAsJsonArray();
        if (isPlayerRegistered(username)) return;
        JsonObject user = new JsonObject();
        String hashed = PassManager.encrypt(password);
        user.addProperty("user", username);
        user.addProperty("password", hashed);
        users.add(user);
        saveDatabase();
    }

    public static boolean isPasswordCorrect(String username, String password) {
        JsonObject user = getPlayer(username);
        if (user == null) return false;
        String hashed = user.get("password").getAsString();
        return PassManager.verify(password, hashed);
    }

    public static boolean sessionVerify(String username, String ip) {
        JsonObject user = getPlayer(username);
        if (user == null) return false;
        JsonElement sip = user.get("session-ip");
        JsonElement stimestamp = user.get("session-timestamp");

        if (sip == null || stimestamp == null) return false; // for compatibility with 1.1.x
        if (!sip.getAsString().equals(ip)) return false;

        LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(stimestamp.getAsString());
        } catch (Exception e) {
            // Date is incorrect
            return false;
        }

        long duration = Duration.between(parsed, LocalDateTime.now()).toHours();
        return duration <= ConfigManager.getInt("sessions-valid-hours");
    }

    public static void sessionCreate(String username, String ip) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("session-ip", ip);
        user.addProperty("session-timestamp", LocalDateTime.now().toString());
        saveDatabase();
    }

    public static void sessionDestroy(String username) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("session-ip", "");
        user.addProperty("session-timestamp", "");
        saveDatabase();
    }

    public static void setPassword(String username, String password) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        String hashed = PassManager.encrypt(password);
        user.addProperty("password", hashed);
        saveDatabase();
    }

    private static void convertDatabase(int version) {
        if (version == -1) {
            // Convert the old database to a new format.
            AuthMod.LOGGER.info("[SimplerAuth] Outdated database! The error above is normal. Converting to the new format, this may take a while.");
            JsonArray users = new JsonArray();

            // Reload database in the old format
            try {
                BufferedReader bufferedReader = Files.newReader(DBFILE, StandardCharsets.UTF_8);
                users = GSON.fromJson(bufferedReader, JsonArray.class);
            } catch (Exception e) {
                AuthMod.LOGGER.error(e);
            }

            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                String password = user.get("password").getAsString();
                String hashed = PassManager.encrypt(password);
                user.addProperty("password", hashed);
            }

            JsonObject newdb = new JsonObject();
            newdb.addProperty("version", VERSION);
            newdb.add("users", users);
            db = newdb;
            AuthMod.LOGGER.info("[SimplerAuth] Finished converting the database.");
            saveDatabase();
        }
    }
}
