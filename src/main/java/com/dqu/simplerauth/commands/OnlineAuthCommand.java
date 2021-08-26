package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.dqu.simplerauth.listeners.OnOnlineAuthChanged;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OnlineAuthCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onlineauth")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    executeEnable(player, source, null);
                    return 1;
                })
                .then(literal("enable")
                        .then(argument("password", StringArgumentType.word())
                                .executes(ctx -> {
                                    String password = StringArgumentType.getString(ctx, "password");
                                    ServerCommandSource source = ctx.getSource();
                                    ServerPlayerEntity player = source.getPlayer();
                                    executeEnable(player, source, password);
                                    return 1;
                                })
                        )
                )
                .then(literal("disable")
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity player = source.getPlayer();
                            executeDisable(player, source, null);
                            return 1;
                        })
                        .then(argument("password", StringArgumentType.word())
                                .executes(ctx -> {
                                    String password = StringArgumentType.getString(ctx, "password");
                                    ServerCommandSource source = ctx.getSource();
                                    ServerPlayerEntity player = source.getPlayer();
                                    executeDisable(player, source, password);
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void executeEnable(ServerPlayerEntity player, ServerCommandSource source, @Nullable String password) {
        String username = player.getEntityName();

        if (checkFeatureDisabled(source)) {
            return;
        }

        if (!DbManager.isPlayerRegistered(username)) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.notregistered"), false);
            return;
        } else if (DbManager.getUseOnlineAuth(username)) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
            return;
        } else if (!AuthMod.doesMinecraftAccountExist(username)) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.cannotenable"), false);
            return;
        }

        if (password != null) {
            if (DbManager.isPasswordCorrect(username, password)) {
                DbManager.setUseOnlineAuth(username, true);
                OnOnlineAuthChanged.onEnabled(player);
                source.sendFeedback(LangManager.getLiteralText("command.onlineauth.enabled"), false);
                if (DbManager.getTwoFactorEnabled(username)) {
                    DbManager.setTwoFactorEnabled(username, false);
                    source.sendFeedback(LangManager.getLiteralText("command.2fa.changed"), false);
                    PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "2fa", "false");
                }
                PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "onlineAuth", "true");
            } else {
                source.sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
            }
        } else {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.warning"), false);
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.confirmenable"), false);
        }
    }

    private static void executeDisable(ServerPlayerEntity player, ServerCommandSource source, @Nullable String password) {
        String username = player.getEntityName();

        if (checkFeatureDisabled(source)) {
            return;
        }

        if (!DbManager.isPlayerRegistered(username)) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.notregistered"), false);
            return;
        } else if (!DbManager.getUseOnlineAuth(username)) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadydisabled"), false);
            return;
        }

        if (password != null) {
            if (DbManager.isPasswordCorrect(username, password)) {
                DbManager.setUseOnlineAuth(username, false);
                OnOnlineAuthChanged.onDisabled(player);
                source.sendFeedback(LangManager.getLiteralText("command.onlineauth.disabled"), false);
                if (DbManager.getTwoFactorEnabled(username)) {
                    DbManager.setTwoFactorEnabled(username, false);
                    source.sendFeedback(LangManager.getLiteralText("command.2fa.changed"), false);
                    PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "2fa", "false");
                }
                PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "onlineAuth", "false");
            } else {
                source.sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
            }
        } else {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.warning"), false);
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.confirmdisable"), false);
        }
    }

    private static boolean checkFeatureDisabled(ServerCommandSource source) {
        String authtype = ConfigManager.getAuthType();
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");

        if (!authtype.equals("local")) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.globaltype"), false);
            return true;
        } else if (!optionalOnlineAuth) {
            source.sendFeedback(LangManager.getLiteralText("command.onlineauth.featuredisabled"), false);
            return true;
        }

        return false;
    }
}
