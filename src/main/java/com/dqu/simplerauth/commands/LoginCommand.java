package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.DbManager;
import com.dqu.simplerauth.PlayerObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

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

                    if (!DbManager.isPlayerRegistered(username)) {
                        ctx.getSource().sendFeedback(new TranslatableText("command.login.notregistered"), false);
                        return 1;
                    }

                    if (DbManager.isPasswordCorrect(username, password)) {
                        PlayerObject playerObject = AuthMod.playerManager.get(player);
                        playerObject.authenticate();
                        if (!player.isCreative()) player.setInvulnerable(false);
                        ctx.getSource().sendFeedback(new TranslatableText("command.login.success"), false);
                    } else {
                        ctx.getSource().sendFeedback(new TranslatableText("command.login.wrongpassword"), false);
                    }

                    return 1;
                })
            )
        );
    }
}
