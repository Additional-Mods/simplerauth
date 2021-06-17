package com.dqu.authmod;

import com.dqu.authmod.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class AuthMod implements ModInitializer {
    public static PlayerManager playerManager = new PlayerManager();

    @Override
    public void onInitialize() {
        DbManager.loadDatabase();
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LoginCommand.registerCommand(dispatcher);
            RegisterCommand.registerCommand(dispatcher);
        }));
    }
}
