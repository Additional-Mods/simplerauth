// Taken from https://github.com/NikitaCartes/EasyAuth
package com.dqu.simplerauth;

import carpet.patches.EntityPlayerMPFake;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public class CarpetHelper {
    /**
     * Checks if player is actually a fake one.
     *
     * @param player player to check
     * @return true if it's fake, otherwise false
     */
    public static boolean isPlayerFake(PlayerEntity player) {
        if (FabricLoader.getInstance().isModLoaded("carpet")) {
            return player instanceof EntityPlayerMPFake;
        } else return false;
    }
}