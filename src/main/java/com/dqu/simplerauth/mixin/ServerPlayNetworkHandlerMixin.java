package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.listeners.OnClickSlot;
import com.dqu.simplerauth.listeners.OnGameMessage;
import com.dqu.simplerauth.listeners.OnPlayerAction;
import com.dqu.simplerauth.listeners.OnPlayerMove;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (!OnPlayerMove.canMove(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (OnPlayerAction.canInteract(this.player)) return;

        ci.cancel();

        if (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
            /*
                Updates players main hand slot to prevent desync
                This action only gets triggered when dropping from the main hand
             */
            ItemStack stack = this.player.getStackInHand(Hand.MAIN_HAND);
            Packet<ClientPlayPacketListener> packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, 1, this.player.getInventory().getSlotWithStack(stack), stack);
            this.sendPacket(packet1);
        } else {
            /*
                Sends a block update packet to the client
                Prevents desync between client and server when breaking or placing blocks
            */
            BlockPos blockPos = packet.getPos();
            Packet<ClientPlayPacketListener> packet1 = new BlockUpdateS2CPacket(this.player.world, blockPos);
            this.sendPacket(packet1);
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (!OnGameMessage.canSendMessage(this.player, packet.getChatMessage())) {
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    public void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (OnClickSlot.canClickSlot(this.player)) return;
        ci.cancel();

        int slot = packet.getSlot();
        if (slot < 0) return; // Clicked outside of the inventory

        ItemStack stack = this.player.getInventory().getStack(slot);
        // ^ packet.getStack() can cause desync

        // Updates clicked slot and the cursor to prevent desync

        Packet<ClientPlayPacketListener> packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, 1, slot, stack);
        Packet<ClientPlayPacketListener> packet2 = new ScreenHandlerSlotUpdateS2CPacket(-1, 1, -1, ItemStack.EMPTY);

        this.sendPacket(packet1); // Updates inventory slot
        this.sendPacket(packet2); // Updates cursor
    }
    
    @Inject(method = "onCreativeInventoryAction", at = @At("HEAD"), cancellable = true)
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        if (OnClickSlot.canClickSlot(this.player)) return;
        ci.cancel();

        int slot = packet.getSlot();
        if (slot < 0) return;

        ItemStack stack = this.player.getInventory().getStack(slot);
        // ^ packet.getStack() can cause desync

        // Updates clicked slot and the cursor to prevent desync

        Packet<ClientPlayPacketListener> packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, 1, slot, stack);
        Packet<ClientPlayPacketListener> packet2 = new ScreenHandlerSlotUpdateS2CPacket(-1, 1, -1, ItemStack.EMPTY);

        this.sendPacket(packet1); // Updates inventory slot
        this.sendPacket(packet2); // Updates cursor
    }
}
