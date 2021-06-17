package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.listeners.OnGameMessage;
import com.dqu.simplerauth.listeners.OnPlayerAction;
import com.dqu.simplerauth.listeners.OnPlayerMove;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        boolean canMove = OnPlayerMove.canMove((ServerPlayNetworkHandler) (Object) this);
        if (!canMove) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        boolean canInteract = OnPlayerAction.canInteract((ServerPlayNetworkHandler) (Object) this);
        if (!canInteract) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        boolean canSendMessage = OnGameMessage.canSendMessage((ServerPlayNetworkHandler) (Object) this, packet);
        if (!canSendMessage) {
            ci.cancel();
        }
    }
}
