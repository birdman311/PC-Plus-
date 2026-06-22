package com.Birdman311.pcmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.util.InputMappings;

public class KeyInit {
    public static KeyBinding multiSelectKey;
    public static KeyBinding favoriteKey;

    public static void register() {
        multiSelectKey = new KeyBinding(
            "key.pcmod.multiselect",
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.categories.pcmod"
        );
        ClientRegistry.registerKeyBinding(multiSelectKey);

        favoriteKey = new KeyBinding(
            "key.pcmod.favorite",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.pcmod"
        );
        ClientRegistry.registerKeyBinding(favoriteKey);
    }
}
