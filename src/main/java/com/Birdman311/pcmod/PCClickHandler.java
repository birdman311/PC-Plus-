package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import com.pixelmonmod.pixelmon.client.gui.pc.PokemonScreen;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
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

            if (position == null) {
                Pokemon heldPokemon = null;
                
                for (Field f : PokemonScreen.class.getDeclaredFields()) {
                    if (f.getType() == Pokemon.class) {
                        f.setAccessible(true);
                        Pokemon p = (Pokemon) f.get(pcScreen);
                        if (p != null) {
                            heldPokemon = p;
                            break;
                        }
                    }
                }

                if (heldPokemon != null && FavoriteManager.isFavorite(heldPokemon.getUUID())) {
                    Field leftField = PokemonScreen.class.getDeclaredField("pcLeft");
                    leftField.setAccessible(true);
                    int pcLeft = (int) leftField.get(pcScreen);

                    Field topField = PokemonScreen.class.getDeclaredField("pcTop");
                    topField.setAccessible(true);
                    int pcTop = (int) topField.get(pcScreen);

                    if (mouseX >= pcLeft + 200 && mouseX <= pcLeft + 260 && mouseY >= pcTop + 120 && mouseY <= pcTop + 180) {
                        mc.player.sendMessage(new StringTextComponent(TextFormatting.RED + "You cannot release a Favorited Pokémon! Unfavorite it first (CTRL + R)."), mc.player.getUUID());
                        mc.player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
                        event.setCanceled(true);
                    }
                }
                return; 
            }

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

    @SubscribeEvent
    public static void onKeyPress(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (KeyInit.favoriteKey.isActiveAndMatches(InputMappings.getKey(event.getKeyCode(), event.getScanCode()))) {
            PCScreen pcScreen = (PCScreen) screen;

            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

            try {
                StoragePosition position = pcScreen.getPosAt(mouseX, mouseY);
                if (position == null) return; 

                Field storageField = PokemonScreen.class.getDeclaredField("storage");
                storageField.setAccessible(true);
                PCStorage pc = (PCStorage) storageField.get(pcScreen);
                if (pc == null) return;

                Pokemon pokemon = pc.getBox(position.box).get(position.order);
                if (pokemon == null) return; 

                FavoriteManager.toggleFavorite(pokemon.getUUID());
                
                mc.player.playSound(net.minecraft.util.SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.0f);
                event.setCanceled(true);

            } catch (Exception e) {
                System.err.println("[PCMod] Error handling favorite key press: " + e.getMessage());
            }
        }
    }
}