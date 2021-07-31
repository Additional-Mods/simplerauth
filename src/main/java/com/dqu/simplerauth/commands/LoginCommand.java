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
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LoginCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("login")
            .then(argument("password", StringArgumentType.word())
                .executes(ctx -> {
                    String password = StringArgumentType.getString(ctx, "password");
                    String username = ctx.getSource().getPlayer().getEntityName();
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    PlayerObject playerObject = AuthMod.playerManager.get(player);
                    String passwordtype = ConfigManager.getAuthType();

                    if (playerObject.isAuthenticated()) {
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.login.alreadylogged"), false);
                        return 1;
                    }

                    if (passwordtype.equals("local")) {
                        // Local Password Authentication

                        if (!DbManager.isPlayerRegistered(username)) {
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.login.notregistered"), false);
                            return 1;
                        }

                        if (DbManager.isPasswordCorrect(username, password)) {
                            playerObject.authenticate(player);
                            if (ConfigManager.getBoolean("sessions-enabled"))
                                DbManager.sessionCreate(player.getEntityName(), player.getIp());
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
                        } else {
                            player.networkHandler.disconnect(LangManager.getLiteralText("command.general.notmatch"));
                        }
                    } else if (passwordtype.equals("global")) {
                        // Global Password Authentication
                        String globalPassword = ConfigManager.getString("global-password");

                        if (password.equals(globalPassword)) {
                            playerObject.authenticate(player);
                            if (ConfigManager.getBoolean("sessions-enabled"))
                                DbManager.sessionCreate(player.getEntityName(), player.getIp());
                            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
                        } else {
                            player.networkHandler.disconnect(LangManager.getLiteralText("command.general.notmatch"));
                        }
                    } else {
                        // Config setup is wrong, kick the player
                        player.networkHandler.disconnect(LangManager.getLiteralText("config.incorrect"));
                    }

                    boolean hideposition = ConfigManager.getBoolean("hide-position");
                    if (hideposition) {
                        Vec3d pos = DbManager.getPosition(username);
                        if (pos != null)
                            player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
                    }

                    return 1;
                })
            )
        );
    }
}
