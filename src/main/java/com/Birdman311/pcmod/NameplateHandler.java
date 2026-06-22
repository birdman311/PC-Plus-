package com.Birdman311.pcmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PCMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NameplateHandler {

    @SubscribeEvent
    public static void onRenderNameplate(RenderNameplateEvent event) {
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        if (!ModUserTracker.isModUser(player.getUUID())) return;

        ITextComponent content = event.getContent();
        if (content == null) return;

        IFormattableTextComponent marked = new StringTextComponent("");
        marked.append(content);
        marked.append(new StringTextComponent(TextFormatting.GREEN + " +"));
        event.setContent(marked);
    }
}
