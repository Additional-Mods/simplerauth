package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.PlayerObject;
import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.dqu.simplerauth.managers.TwoFactorManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
                        String username = ctx.getSource().getPlayer().getEntityName();
                        if (!DbManager.getTwoFactorEnabled(username) && !ConfigManager.getAuthType().equals("2fa")) {
                            return login(ctx);
                        } else if (DbManager.getUseOnlineAuth(username) || ConfigManager.getAuthType().equals("2fa")) return loginCode(ctx);
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.2fa.enter"), false);
                        return 0;
                    })
                    .then(argument("code", StringArgumentType.word()).executes(ctx -> {
                        String username = ctx.getSource().getPlayer().getEntityName();
                        String code = StringArgumentType.getString(ctx, "code");
                        if (!DbManager.getTwoFactorEnabled(username)) {
                            return login(ctx);
                        }
                        return login(ctx, code);
                    }))
            )
        );
    }

    private static int loginCode(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String code = StringArgumentType.getString(ctx, "password");
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (TwoFactorManager.validate(player, code)) {
            PlayerObject playerObject = AuthMod.playerManager.get(player);
            PlayerAuthEvents.PLAYER_LOGIN.invoker().onPlayerLogin(player, "loginCommand");
            playerObject.authenticate();
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
            return 1;
        } else {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.2fa.incorrect"), false);
            if (ConfigManager.getAuthType().equals("2fa")) player.networkHandler.disconnect(LangManager.getLiteralText("command.general.2fa.incorrect"));
            return 0;
        }
    }

    private static int login(CommandContext<ServerCommandSource> ctx, String pin) throws CommandSyntaxException {
        if (TwoFactorManager.validate(ctx.getSource().getPlayer(), pin)) {
            return login(ctx);
        } else {
            if (ConfigManager.getAuthType().equals("2fa")) ctx.getSource().getPlayer().networkHandler.disconnect(LangManager.getLiteralText("command.general.2fa.incorrect"));
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.general.2fa.incorrect")).create();
        }
    }

    private static int login(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        PlayerObject playerObject = AuthMod.playerManager.get(player);

        if (playerObject.isAuthenticated()) {
            throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.login.alreadylogged")).create();
        }

        String password = StringArgumentType.getString(ctx, "password");
        String username = player.getEntityName();
        
        if (!isPasswordCorrect(player, password)) {
            // TODO: fails and kick after x fails
            PlayerAuthEvents.PLAYER_LOGIN_FAIL.invoker().onPlayerLoginFail(player, 1);
            String message = ConfigManager.getAuthType().equals("2fa") ? "command.general.2fa.incorrect" : "command.general.notmatch";
            player.networkHandler.disconnect(LangManager.getLiteralText(message));
            return 0;
        }
        playerObject.authenticate();
        PlayerAuthEvents.PLAYER_LOGIN.invoker().onPlayerLogin(player, "loginCommand");
        if (ConfigManager.getBoolean("sessions-enabled")) {
            DbManager.sessionCreate(player.getEntityName(), player.getIp());
        }
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.authenticated"), false);
        if (ConfigManager.getBoolean("hide-position")) {
            Vec3d pos = DbManager.getPosition(username);
            if (pos != null)
                player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
        }
        return 1;
    }

    private static boolean isPasswordCorrect(ServerPlayerEntity player, String password) throws CommandSyntaxException {
        String username = player.getEntityName();
        switch (ConfigManager.getAuthType()) {
            case "local" -> {
                // Local Password Authentication
                if (!DbManager.isPlayerRegistered(username)) {
                    throw new SimpleCommandExceptionType(LangManager.getLiteralText("command.login.notregistered")).create();
                }
                if (DbManager.isPasswordCorrect(username, password)) {
                    return true;
                }
            }
            case "global" -> {
                // Global Password Authentication
                if (password.equals(ConfigManager.getString("global-password"))) {
                    return true;
                }
            }
            case "2fa" -> {
                // Optional 2FA Only Authentication
                if (TwoFactorManager.validate(player, password)) {
                    return true;
                }
            }
            default -> {
                // Config setup is wrong
                throw new SimpleCommandExceptionType(LangManager.getLiteralText("config.incorrect")).create();
            }
        }
        return false;
    }
}
