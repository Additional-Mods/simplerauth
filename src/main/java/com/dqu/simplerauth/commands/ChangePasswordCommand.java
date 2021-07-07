package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChangePasswordCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("changepassword")
            .then(argument("oldPassword", StringArgumentType.word())
                .then(argument("newPassword", StringArgumentType.word())
                    .executes(ctx -> {
                        String oldPassword = StringArgumentType.getString(ctx, "oldPassword");
                        String newPassword = StringArgumentType.getString(ctx, "newPassword");
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        String username = player.getEntityName();
                        String authtype = ConfigManager.getAuthType();

                        if (authtype.equals("global")) {
                            if (player.hasPermissionLevel(4)) {
                                // If a player is an operator, and the password type is global - change the global password
                                String password = ConfigManager.getString("global-password");
                                if (password.equals(oldPassword)) {
                                    ConfigManager.setString("global-password", newPassword);
                                }
                            }
                            return 1;
                        } else if (authtype.equals("none")) {
                            player.networkHandler.disconnect(LangManager.getLiteralText("config.incorrect"));
                        }

                        if (!DbManager.isPlayerRegistered(username)) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.changepassword.notregistered"), false);
                            return 1;
                        }

                        if(!DbManager.isPasswordCorrect(username, oldPassword)) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
                            return 1;
                        }

                        DbManager.setPassword(username, newPassword);
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.changepassword.success"), false);
                        return 1;

                    })
                )
            )
        );
    }
}
