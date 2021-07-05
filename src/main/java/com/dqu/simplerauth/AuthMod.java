package com.dqu.simplerauth;

import com.dqu.simplerauth.commands.*;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.managers.PlayerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static PlayerManager playerManager = new PlayerManager();

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig(); // Loads config file
        DbManager.loadDatabase(); // Loads password database
        LangManager.loadTranslations("en"); // Loads translations
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LoginCommand.registerCommand(dispatcher);
            RegisterCommand.registerCommand(dispatcher);
            ChangePasswordCommand.registerCommand(dispatcher);
        }));
    }
}
