// Taken from https://github.com/NikitaCartes/EasyAuth
package com.dqu.simplerauth;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.player.PlayerEntity;

public class CarpetHelper {
    /**
     * Checks if player is actually a fake one.
     *
     * @param player player to check
     * @return true if it's fake, otherwise false
     */
    public static boolean isPlayerFake(PlayerEntity player) {
        return player instanceof EntityPlayerMPFake;
    }
}