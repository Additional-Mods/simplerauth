package com.dqu.authmod.commands;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.DbManager;
import com.dqu.authmod.PlayerObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

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
                        ctx.getSource().sendFeedback(new LiteralText("§cВы ещё не зарегестрированы! Используйте: /register <пароль> <пароль>"), false);
                        return 1;
                    }

                    if (DbManager.isPasswordCorrect(username, password)) {
                        PlayerObject playerObject = AuthMod.playerManager.get(player);
                        playerObject.authenticate();
                        if (!player.isCreative()) player.setInvulnerable(false);
                        ctx.getSource().sendFeedback(new LiteralText("§aВы вошли в ваш аккаунт."), false);
                    } else {
                        ctx.getSource().sendFeedback(new LiteralText("§cНеверный пароль!"), false);
                    }

                    return 1;
                })
            )
        );
    }
}