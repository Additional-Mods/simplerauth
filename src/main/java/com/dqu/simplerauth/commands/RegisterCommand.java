package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.PlayerObject;
import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RegisterCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("register")
            .then(argument("password", StringArgumentType.word())
                .then(argument("repeatPassword", StringArgumentType.word())
                    .executes(ctx -> register(ctx))
                )
            )
        );
    }

    private static int register(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String authtype = ConfigManager.getAuthType();

        if (authtype.equals("none")) {
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("config.incorrect")).create();
        }
        if (!authtype.equals("local")) {
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.register.globaltype")).create();
        }
        
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String username = player.getEntityName();
        
        if (DbManager.isPlayerRegistered(username)) {
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.register.alreadyregistered")).create();
        }
        
        String password = StringArgumentType.getString(ctx, "password");
        String passwordRepeat = StringArgumentType.getString(ctx, "repeatPassword");

        if (!password.equals(passwordRepeat)) {
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.general.notmatch")).create();
        }

        try {
            Pattern regex = Pattern.compile(ConfigManager.getString("password-regex"));
            Matcher matcher = regex.matcher(password);
            if (!matcher.matches()) {
                throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.register.passweak")).create();
            }
        } catch (Exception e) {
            AuthMod.LOGGER.error(LangManager.get("config.incorrect"));
            AuthMod.LOGGER.warn("Skipping regex password validation as the config is incorrect.");
        }

        DbManager.addPlayerDatabase(username, password);
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        playerObject.authenticate();
        PlayerAuthEvents.PLAYER_REGISTER.invoker().onPlayerRegister(player);
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
        return 1;
    }
}
