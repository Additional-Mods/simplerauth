package com.dqu.simplerauth.mixin;

import com.dqu.simplerauth.AuthMod;
import com.dqu.simplerauth.managers.ConfigManager;
import com.dqu.simplerauth.managers.DbManager;
import com.mojang.authlib.GameProfileRepository;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UserCache.class)
public abstract class UserCacheMixin {
    @Shadow
    private static boolean shouldUseRemote() {
        return false;
    }

    // Without this, there might be issues with offline players
    /** @noinspection UnresolvedMixinReference */
    @SuppressWarnings("target")
    @Redirect(method = {
            "method_14509(Lcom/mojang/authlib/GameProfileRepository;Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;",
            "method_14509(Lcom/mojang/authlib/GameProfileRepository;Ljava/lang/String;)Ljava/util/Optional;"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/UserCache;shouldUseRemote()Z"))
    private static boolean useOnlineUuid(GameProfileRepository repository, String name) {
        boolean forcedOnlineAuth = ConfigManager.getBoolean("forced-online-auth");
        boolean optionalOnlineAuth = ConfigManager.getBoolean("optional-online-auth");
        if (!shouldUseRemote() || (!forcedOnlineAuth && !optionalOnlineAuth)) {
            return false;
        }

        if (forcedOnlineAuth) return AuthMod.doesMinecraftAccountExist(name);
        else return DbManager.getUseOnlineAuth(name);
    }
}
