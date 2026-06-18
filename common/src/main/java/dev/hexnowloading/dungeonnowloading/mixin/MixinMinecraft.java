package dev.hexnowloading.dungeonnowloading.mixin;

import dev.hexnowloading.dungeonnowloading.Constants;
import dev.hexnowloading.dungeonnowloading.client.MimiclingPickBlockHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        
        Constants.LOG.info("This line is printed by an example mod common mixin!");
        Constants.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void dnl$pickBlockWithMimicling(CallbackInfo info) {
        if (MimiclingPickBlockHandler.handlePickBlock((Minecraft)(Object)this)) {
            info.cancel();
        }
    }
}
