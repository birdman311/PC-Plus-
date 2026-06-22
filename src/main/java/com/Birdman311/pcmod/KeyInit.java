package com.Birdman311.pcmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static KeyBinding multiSelectKey;

    public static void register() {
        // Creates the keybind: "Multi-Select", defaults to Left Control, in a "PC Plus+" category
        multiSelectKey = new KeyBinding(
            "key.pcmod.multiselect", 
            GLFW.GLFW_KEY_LEFT_CONTROL, 
            "key.categories.pcmod"
        );
        ClientRegistry.registerKeyBinding(multiSelectKey);
    }
}