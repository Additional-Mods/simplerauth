package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.DbManager;
import com.dqu.simplerauth.LangManager;
import com.dqu.simplerauth.PlayerObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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

                    if (playerObject.isAuthenticated()) {
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.login.alreadylogged"), false);
                        return 1;
                    }

                    if (!DbManager.isPlayerRegistered(username)) {
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.login.notregistered"), false);
                        return 1;
                    }

                    if (DbManager.isPasswordCorrect(username, password)) {
                        playerObject.authenticate();
                        if (!player.isCreative()) player.setInvulnerable(false);
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.login.success"), false);
                    } else {
                        player.networkHandler.disconnect(LangManager.getLiteralText("command.login.wrongpassword"));
                    }

                    return 1;
                })
            )
        );
    }
}
