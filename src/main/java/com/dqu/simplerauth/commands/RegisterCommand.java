package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.DbManager;
import com.dqu.simplerauth.PlayerObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RegisterCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("register")
            .then(argument("password", StringArgumentType.word())
                .then(argument("repeatPassword", StringArgumentType.word())
                    .executes(ctx -> {
                        String password = StringArgumentType.getString(ctx, "password");
                        String passwordRepeat = StringArgumentType.getString(ctx, "password");
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        String username = player.getEntityName();

                        if (DbManager.isPlayerRegistered(username)) {
                            ctx.getSource().sendFeedback(new LiteralText("§cВы уже зарегестрированы! Используйте /login"), false);
                            return 1;
                        }

                        if (!password.equals(passwordRepeat)) {
                            ctx.getSource().sendFeedback(new LiteralText("§cПароли не совпадают!"), false);
                            return 1;
                        }

                        DbManager.addPlayerDatabase(username, password);
                        PlayerObject playerObject = AuthMod.playerManager.get(player);
                        playerObject.authenticate();
                        if (!player.isCreative()) player.setInvulnerable(false);
                        ctx.getSource().sendFeedback(new LiteralText("§aУспешно зарегестрировано!"), false);
                        return 1;
                    })
                )
            )
        );
    }
}
