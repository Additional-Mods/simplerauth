package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    public static final int VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PATH = FabricLoader.getInstance().getConfigDir().resolve("simplerauth-config.json").toString();
    private static final File DBFILE = new File(PATH);
    private static JsonObject db = new JsonObject();

    public static void loadConfig() {
        if (!DBFILE.exists()) {
            db.addProperty("version", VERSION);

            db.addProperty("sessions-enabled", true);
            db.addProperty("sessions-valid-hours", "6");
            db.addProperty("skip-online-auth", true);
            db.addProperty("password-type", "local");
            db.addProperty("global-password", "123456");

            saveDatabase();
        }

        try {
            BufferedReader bufferedReader = Files.newReader(DBFILE, StandardCharsets.UTF_8);
            db = GSON.fromJson(bufferedReader, JsonObject.class);
        } catch (Exception e) {
            AuthMod.LOGGER.error(e);
        }
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

    public static boolean getBoolean(String key) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("getBoolean was called but config file doesn't exist!");
            return false;
        }
        return db.get(key).getAsBoolean();
    }

    public static void setBoolean(String key, Boolean value) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("setString was called but config file doesn't exist!");
            return;
        }
        db.addProperty(key, value);
        saveDatabase();
    }

    public static int getInt(String key) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("getInt was called but config file doesn't exist!");
            return 0;
        }
        return db.get(key).getAsInt();
    }

    public static void setInt(String key, Integer value) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("setString was called but config file doesn't exist!");
            return;
        }
        db.addProperty(key, value);
        saveDatabase();
    }

    public static String getString(String key) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("getString was called but config file doesn't exist!");
            return "Not found";
        }
        return db.get(key).getAsString();
    }

    public static void setString(String key, String value) {
        if (!DBFILE.exists()) {
            AuthMod.LOGGER.warn("setString was called but config file doesn't exist!");
            return;
        }
        db.addProperty(key, value);
        saveDatabase();
    }

    public static String getAuthType() {
        String authtype = getString("password-type");
        if (authtype.equalsIgnoreCase("local")) return "local";
        return authtype.equalsIgnoreCase("global") ? "global" : "none";
    }

}
