package com.dqu.simplerauth.commands;

import com.dqu.simplerauth.api.event.PlayerAuthEvents;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.dqu.simplerauth.managers.LangManager;
import com.dqu.simplerauth.managers.TwoFactorManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
                            if(!ConfigManager.getAuthType().equals("2fa")) {
                                ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enable"), false);
                                return 1;
                            }
                            return pair(ctx, 0);
                        })
                        .then(argument("string", StringArgumentType.word())
                                .executes(ctx -> pair(ctx, 1))
                                .then(argument("code", StringArgumentType.word()).executes(ctx -> validate(ctx, 0)))
                        )
                )
                .then(literal("disable")
                        .executes(ctx -> {
                            if(!ConfigManager.getAuthType().equals("2fa")) {
                                ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.disable"), false);
                                return 1;
                            }
                            return disable(ctx, 0);
                        })
                        .then(argument("password", StringArgumentType.word()).executes(ctx -> disable(ctx, 1)))
                )
        );
    }

    private static int pair(CommandContext<ServerCommandSource> ctx, int stage) throws CommandSyntaxException {
        if (stage == 1) {
            if (ConfigManager.getAuthType().equals("2fa")) return validate(ctx, 1);
        }
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String username = player.getEntityName();
        boolean shouldCheckPassword = (stage == 0) && !ConfigManager.getAuthType().equals("2fa");
        if (DbManager.getTwoFactorEnabled(username)) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadyenabled"), false);
            return 0;
        }
        if (shouldCheckPassword && !DbManager.isPasswordCorrect(username, StringArgumentType.getString(ctx, "string"))) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
            return 0;
        }
        String url = TwoFactorManager.pair(player);
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enable.pair").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))), false);
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enable.validate"), false);
        return 1;
    }

    private static int validate(CommandContext<ServerCommandSource> ctx, int stage) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String username = player.getEntityName();
        if (stage == 0 && !DbManager.isPasswordCorrect(username, StringArgumentType.getString(ctx, "string"))) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
            return 0;
        }
        String code = (stage == 0) ? StringArgumentType.getString(ctx, "code") : StringArgumentType.getString(ctx, "string");
        if (!TwoFactorManager.validate(player, code)) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.2fa.incorrect"), false);
            return 0;
        }

        DbManager.setTwoFactorEnabled(username, true);
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.enabled"), false);
        PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "2fa", "true");
        return 1;
    }

    private static int disable(CommandContext<ServerCommandSource> ctx, int stage) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String username = player.getEntityName();
        if (stage == 1 && !DbManager.isPasswordCorrect(username, StringArgumentType.getString(ctx, "password"))) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.general.notmatch"), false);
            return 0;
        }
        if (!DbManager.getTwoFactorEnabled(username)) {
            ctx.getSource().sendFeedback(LangManager.getLiteralText("command.onlineauth.alreadydisabled"), false);
            return 0;
        }
        DbManager.setTwoFactorEnabled(username, false);
        ctx.getSource().sendFeedback(LangManager.getLiteralText("command.2fa.disabled"), false);
        PlayerAuthEvents.PLAYER_ACCOUNT_MODIFIED.invoker().onPlayerAccountModified(player, "2fa", "false");
        return 1;
    }
}
