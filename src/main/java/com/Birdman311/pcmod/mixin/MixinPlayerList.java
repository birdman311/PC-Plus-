package com.Birdman311.pcmod.mixin;

import com.Birdman311.pcmod.ModUserTracker;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerTabOverlayGui.class)
public class MixinPlayerList {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void pcmod$markMcp(NetworkPlayerInfo info, CallbackInfoReturnable<ITextComponent> cir) {
        pcmod$mark(info, cir);
    }

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true, require = 0)
    private void pcmod$markMojang(NetworkPlayerInfo info, CallbackInfoReturnable<ITextComponent> cir) {
        pcmod$mark(info, cir);
    }

    @Unique
    private void pcmod$mark(NetworkPlayerInfo info, CallbackInfoReturnable<ITextComponent> cir) {
        if (info == null || info.getProfile() == null) return;
        UUID id = info.getProfile().getId();
        if (!ModUserTracker.isModUser(id)) return;

        ITextComponent original = cir.getReturnValue();
        if (original == null) return;

        IFormattableTextComponent result = new StringTextComponent("");
        result.append(original);
        result.append(new StringTextComponent(TextFormatting.GREEN + " +"));
        cir.setReturnValue(result);
    }
}
