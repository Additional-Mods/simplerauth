package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.PlayerObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RegisterCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("register")
            .then(argument("password", StringArgumentType.word())
                .then(argument("repeatPassword", StringArgumentType.word())
                    .executes(ctx -> {
                        String password = StringArgumentType.getString(ctx, "password");
                        String passwordRepeat = StringArgumentType.getString(ctx, "repeatPassword");
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        String username = player.getEntityName();
                        String authtype = ConfigManager.getAuthType();

                        if (authtype.equals("global")) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.register.globaltype"), false);
                            return 1;
                        } else if (authtype.equals("none")) {
                            player.networkHandler.disconnect(LangManager.getLiteralText("config.incorrect"));
                        }

                        if (DbManager.isPlayerRegistered(username)) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.register.alreadyregistered"), false);
                            return 1;
                        }

                        if (!password.equals(passwordRepeat)) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
                            return 1;
                        }

                        DbManager.addPlayerDatabase(username, password);
                        PlayerObject playerObject = AuthMod.playerManager.get(player);
                        playerObject.authenticate();
                        if (!player.isCreative()) player.setInvulnerable(false);
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
                        return 1;
                    })
                )
            )
        );
    }
}
