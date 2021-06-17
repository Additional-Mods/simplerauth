package com.dqu.simplerauth;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class DbManager {
    private static final File dbfile = new File("database.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JsonArray db = new JsonArray();

    private static JsonObject findPlayer(String username) {
        JsonObject player = null;
        if (db.size() == 0) return null;
        for (int i = 0; i < db.size(); i++) {
            JsonObject iplayer = db.get(i).getAsJsonObject();
            if (iplayer.get("user").getAsString().equals(username)) {
                player = iplayer;
                break;
            }
        }
        return player;
    }

    public static boolean isPlayerRegistered(String username) {
        return findPlayer(username) != null;
    }

    public static boolean isPasswordCorrect(String username, String password) {
        JsonObject player = findPlayer(username);
        if (player == null) return false;
        return player.get("password").getAsString().equals(password);
    }

    public static void addPlayerDatabase(String username, String password) {
        JsonObject player = new JsonObject();
        player.addProperty("user", username);
        player.addProperty("password", password);
        db.add(player);
        saveDatabase();
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

    public static void loadDatabase() {
        if (!dbfile.exists()) return;
        try {
            BufferedReader bufferedReader = Files.newReader(dbfile, StandardCharsets.UTF_8);
            JsonArray json = gson.fromJson(bufferedReader, JsonArray.class);
            db = json;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
