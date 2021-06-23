package com.dqu.simplerauth;

import com.google.common.io.Files;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class DbManager {
    private static final String path = FabricLoader.getInstance().getConfigDir().resolve("simplerauth-database.json").toString();
    private static final File dbfile = new File(path);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject db = new JsonObject();

    public static void loadDatabase() {
        if (!dbfile.exists()) {
            db.addProperty("version", 1);
            db.add("users", new JsonArray());
            saveDatabase();
        }

        try {
            BufferedReader bufferedReader = Files.newReader(dbfile, StandardCharsets.UTF_8);
            db = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.get("version") == null) {
            // Convert the old database to a new format.
            LogManager.getLogger().info("[SimplerAuth] Outdated database! The error above is normal. Converting to the new format, this may take a while.");
            JsonArray users = new JsonArray();

            // Reload database in the old format
            try {
                BufferedReader bufferedReader = Files.newReader(dbfile, StandardCharsets.UTF_8);
                users = gson.fromJson(bufferedReader, JsonArray.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                String password = user.get("password").getAsString();
                String hashed = PassManager.encrypt(password);
                user.addProperty("password", hashed);
            }

            JsonObject newdb = new JsonObject();
            newdb.addProperty("version", 1);
            newdb.add("users", users);
            db = newdb;
            LogManager.getLogger().info("[SimplerAuth] Finished converting the database.");
            saveDatabase();
        }
    }

    private static void saveDatabase() {
        try {
            BufferedWriter bufferedWriter = Files.newWriter(dbfile, StandardCharsets.UTF_8);
            String json = gson.toJson(db);
            bufferedWriter.write(json);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonObject getPlayer(String username) {
        if (!dbfile.exists()) return null;
        JsonArray users = db.get("users").getAsJsonArray();
        if (users.size() == 0) return null;
        for (int i = 0; i < db.size(); i++) {
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

    public static void setPassword(String username, String password) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        String hashed = PassManager.encrypt(password);
        user.addProperty("password", hashed);
        saveDatabase();
    }
}
