package com.dqu.simplerauth.managers;

import com.dqu.simplerauth.AuthMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {
    private static JsonObject lang = new JsonObject();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MutableText getLiteralText(String key) {
        return Text.literal(get(key));
    }

    public static String get(String key) {
        return lang.get(key) == null ? key : lang.get(key).getAsString();
    }

    public static void loadTranslations(String language) {
        try {
            String path = String.format("assets/simplerauth/lang/%s.json", language);
            InputStream inputStream = AuthMod.class.getClassLoader().getResourceAsStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            lang = GSON.fromJson(bufferedReader, JsonObject.class);
        } catch (Exception e) {
            AuthMod.LOGGER.error("[SimplerAuth] Unable to load translation files!");
        }
    }

}
