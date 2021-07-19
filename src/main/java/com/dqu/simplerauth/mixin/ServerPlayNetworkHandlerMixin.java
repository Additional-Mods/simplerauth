package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.listeners.OnClickSlot;
import com.dqu.simplerauth.listeners.OnGameMessage;
import com.dqu.simplerauth.listeners.OnPlayerAction;
import com.dqu.simplerauth.listeners.OnPlayerMove;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
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
        if (canInteract) return;

        ci.cancel();
        ServerPlayerEntity player = networkHandler.player;

        if (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
            /*
                Updates players main hand slot to prevent desync
                This action only gets triggered when dropping from the main hand
             */
            ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
            Packet packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, player.inventory.getSlotWithStack(stack), stack);
            networkHandler.sendPacket(packet1);
        } else {
            /*
                Sends a block update packet to the client
                Prevents desync between client and server when breaking or placing blocks
            */
            BlockPos blockPos = packet.getPos();
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

    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    public void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler networkHandler = (ServerPlayNetworkHandler) (Object) this;
        boolean canClickSlot = OnClickSlot.canClickSlot(networkHandler);
        if (canClickSlot) return;
        ci.cancel();

        ServerPlayerEntity player = networkHandler.player;
        int slot = packet.getSlot();
        if (slot < 0) return; // Clicked outside of the inventory

        ItemStack stack = player.inventory.getStack(slot);
        // ^ packet.getStack() can cause desync

        // Updates clicked slot and the cursor to prevent desync

        Packet packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, slot, stack);
        Packet packet2 = new ScreenHandlerSlotUpdateS2CPacket(-1, -1, ItemStack.EMPTY);

        networkHandler.sendPacket(packet1); // Updates inventory slot
        networkHandler.sendPacket(packet2); // Updates cursor
    }
    
    @Inject(method = "onCreativeInventoryAction", at = @At("HEAD"), cancellable = true)
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler networkHandler = (ServerPlayNetworkHandler) (Object) this;
        if (OnClickSlot.canClickSlot(networkHandler)) return;
        ci.cancel();

        ServerPlayerEntity player = networkHandler.player;
        int slot = packet.getSlot();
        if (slot < 0) return;

        ItemStack stack = player.inventory.getStack(slot);
        // ^ packet.getStack() can cause desync

        // Updates clicked slot and the cursor to prevent desync

        Packet packet1 = new ScreenHandlerSlotUpdateS2CPacket(-2, slot, stack);
        Packet packet2 = new ScreenHandlerSlotUpdateS2CPacket(-1, -1, ItemStack.EMPTY);

        networkHandler.sendPacket(packet1); // Updates inventory slot
        networkHandler.sendPacket(packet2); // Updates cursor
    }
}
