package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import com.pixelmonmod.pixelmon.client.gui.pc.PokemonScreen;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = PCMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PCClickHandler {

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PCScreen pcScreen = (PCScreen) screen;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        try {
            StoragePosition position = pcScreen.getPosAt(mouseX, mouseY);
            if (position == null) return;

            Field storageField = PokemonScreen.class.getDeclaredField("storage");
            storageField.setAccessible(true);
            PCStorage pc = (PCStorage) storageField.get(pcScreen);
            if (pc == null) return;

            Field boxField = PokemonScreen.class.getDeclaredField("boxNumber");
            boxField.setAccessible(true);
            int boxNumber = (int) boxField.get(pcScreen);

            PCModGuiOverlay.setCurrentBox(boxNumber);

            Pokemon pokemon = pc.getBox(position.box).get(position.order);
            if (pokemon == null) return;

            MultiGrabHandler grabHandler = PCMod.getEventHandler().getHandler(mc.player);

            // Check if our custom key is pressed (Defaults to Left Ctrl)
            boolean isModifierPressed = InputMappings.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(),
                KeyInit.multiSelectKey.getKey().getValue()
            );

            if (grabHandler.isSelected(pokemon)) {
                if (isModifierPressed && event.getButton() == 0) {
                    grabHandler.toggleSelection(pokemon);
                }
                event.setCanceled(true);
                return;
            }

            if (!isModifierPressed || event.getButton() != 0) return;

            PCMod.getEventHandler().onMultiSelectClick(mc.player, pokemon);
            event.setCanceled(true);

        } catch (Exception e) {
            System.err.println("[PCMod] Error handling click: " + e.getMessage());
        }
    }
}