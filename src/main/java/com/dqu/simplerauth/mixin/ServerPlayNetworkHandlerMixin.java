package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.listeners.OnGameMessage;
import com.dqu.simplerauth.listeners.OnPlayerAction;
import com.dqu.simplerauth.listeners.OnPlayerMove;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
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
        ServerPlayNetworkHandler networkHandler = (ServerPlayNetworkHandler) (Object) this;
        boolean canInteract = OnPlayerAction.canInteract(networkHandler);
        if (!canInteract) {
            ci.cancel();
            
            /*
                Sends a block update packet to the client
                Prevents desync between client and server when breaking or placing blocks
            */
            BlockPos blockPos = packet.getPos();
            ServerPlayerEntity player = networkHandler.getPlayer();
            Packet packet1 = new BlockUpdateS2CPacket(player.world, blockPos);
            networkHandler.sendPacket(packet1);
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
