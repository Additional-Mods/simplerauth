package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.PlayerObject;
import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.managers.PlayerManager;
import com.dqu.simplerauth.managers.TwoFactorManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TwoFactorCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("2fa")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    String message = DbManager.getTwoFactorEnabled(player.getEntityName()) ? "command.2fa.disable" : "command.2fa.enable";
                    ctx.getSource().sendFeedback(LangManager.getLiteralText(message), false);
                    return 1;
                })
                .then(literal("enable")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (DbManager.getTwoFactorEnabled(player.getEntityName())) {
                                ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
                                return 0;
                            }
                            return 1;
                        })
                        .then(argument("password", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    String username = player.getEntityName();
                                    if (DbManager.getTwoFactorEnabled(username)) {
                                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
                                        return 0;
                                    }
                                    String password = StringArgumentType.getString(ctx, "password");
                                    if (!DbManager.isPasswordCorrect(username, password)) {
                                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
                                        return 0;
                                    }

                                    String url = TwoFactorManager.pair(player);
                                    ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enable.pair").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))), false);
                                    ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enable.validate"), false);
                                    return 1;
                                })
                                .then(argument("code", StringArgumentType.word()).executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    String username = player.getEntityName();
                                    if (DbManager.getTwoFactorEnabled(username)) {
                                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
                                        return 0;
                                    }
                                    String password = StringArgumentType.getString(ctx, "password");
                                    if (!DbManager.isPasswordCorrect(username, password)) {
                                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
                                        return 0;
                                    }
                                    String code = StringArgumentType.getString(ctx, "code");
                                    if (!TwoFactorManager.validate(player, code)) {
                                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.2fa.incorrect"), false);
                                        return 0;
                                    }

                                    DbManager.setTwoFactorEnabled(username, true);
                                    ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enabled"), false);
                                    return 1;
                                }))
                        )
                )

                .then(literal("disable").then(argument("password", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    String username = player.getEntityName();
                    if (!DbManager.getTwoFactorEnabled(username)) {
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadydisabled"), false);
                        return 0;
                    }
                    String password = StringArgumentType.getString(ctx, "password");
                    if (!DbManager.isPasswordCorrect(username, password)) {
                        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
                        return 0;
                    }

                    DbManager.setTwoFactorEnabled(username, false);
                    ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.disabled"), false);
                    return 1;
                })))
        );
    }
}
