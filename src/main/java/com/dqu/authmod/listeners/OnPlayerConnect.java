package com.dqu.authmod.listeners;

import com.dqu.authmod.AuthMod;
import com.dqu.authmod.PlayerObject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class OnPlayerConnect {
    public static void listen(ServerPlayerEntity player) {
        PlayerObject playerObject = AuthMod.playerManager.get(player);
        player.setInvulnerable(true);
        player.sendMessage(new LiteralText("§9Добро пожаловать! Используйте /login для входа и /register для регистрации!"), false);
    }
}