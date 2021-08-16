package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;
import com.google.common.io.Files;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

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
        JsonObject user = getPlayer(username);
        if (user == null) return false;
        String hash = getHash(username);
        return !PassManager.isUnregistered(hash);
    }

    public static void unregister(String username) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("password", "none");
        if (DbManager.getTwoFactorEnabled(username)) DbManager.setTwoFactorEnabled(username, false);
        saveDatabase();
    }

    public static void addPlayerDatabase(String username, String password) {
        if (isPlayerRegistered(username)) return;
        JsonObject user = getPlayer(username);
        JsonArray users = db.get("users").getAsJsonArray();
        if (user == null) {
            JsonObject newUser  = new JsonObject();
            String hashed = PassManager.encrypt(password);
            newUser.addProperty("user", username);
            newUser.addProperty("password", hashed);
            users.add(newUser);
        } else {
            String hashed = PassManager.encrypt(password);
            user.addProperty("password", hashed);
        }
        saveDatabase();
    }

    public static boolean isPasswordCorrect(String username, String password) {
        JsonObject user = getPlayer(username);
        if (user == null) return false;
        String hashed = getHash(username);
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

    public static void setUseOnlineAuth(String username, boolean onlineAuth) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("online-auth", onlineAuth);
        if (DbManager.getTwoFactorEnabled(username)) DbManager.setTwoFactorEnabled(username, false);
        saveDatabase();
    }

    public static boolean getUseOnlineAuth(String username) {
        JsonObject user = getPlayer(username);
        if (user == null || !user.has("online-auth")) return false;
        return user.get("online-auth").getAsBoolean();
    }

    public static void savePosition(String username, Vec3d position) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("lastPosition", position.toString());
        saveDatabase();
    }

    public static Vec3d getPosition(String username) {
        JsonObject user = getPlayer(username);
        if (user == null || !user.has("lastPosition")) return null;
        String pos = user.get("lastPosition").getAsString();
        if (pos == null) return null;
        String[] spos = pos.replace("(", "").replace(")", "").split(",");
        return new Vec3d(Double.parseDouble(spos[0]), Double.parseDouble(spos[1]), Double.parseDouble(spos[2]));
    }

    public static boolean getTwoFactorEnabled(String username) {
        JsonObject user = getPlayer(username);
        if (user == null || !user.has("2fa")) return false;
        return user.get("2fa").getAsBoolean();
    }

    public static void setTwoFactorEnabled(String username, Boolean value) {
        JsonObject user = getPlayer(username);
        if (user == null) return;
        user.addProperty("2fa", value);
        saveDatabase();
    }

    public static String getHash(String username) {
        JsonObject user = getPlayer(username);
        if (user == null) return null;
        return user.get("password").getAsString();
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
