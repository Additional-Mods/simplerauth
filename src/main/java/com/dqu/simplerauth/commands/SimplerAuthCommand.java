package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.CacheManager;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SimplerAuthCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("simplerauth")
                .then(literal("reload")
                    .executes(ctx -> {
                        ConfigManager.loadConfig();
                        DbManager.loadDatabase();
                        CacheManager.loadCache();
                        LangManager.loadTranslations(ConfigManager.getString("language"));
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("config.reload"), false);
                        return 1;
                    })
                )

                .then(literal("unregister")
                    .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> {
                            String username = StringArgumentType.getString(ctx, "player");
                            DbManager.unregister(username);
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.success"), false);
                            return 1;
                        })
                    )
                )
        );
    }
}
