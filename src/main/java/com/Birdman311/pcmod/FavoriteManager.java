package com.Birdman311.pcmod;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FavoriteManager {

    private static final Set<UUID> favorites = new HashSet<>();

    private static File getSaveFile() {
        Minecraft mc = Minecraft.getInstance();
        String serverName = "UnknownServer";

        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            serverName = "SP_" + mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            serverName = "MP_" + mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9.-]", "_");
        }

        File configDir = new File(mc.gameDirectory, "config/pcmod");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        return new File(configDir, "favorites_" + serverName + ".txt");
    }

    public static void loadFavorites() {
        favorites.clear();
        File file = getSaveFile();
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    favorites.add(UUID.fromString(line.trim()));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[PCMod] Failed to load favorites: " + e.getMessage());
        }
    }

    public static void saveFavorites() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getSaveFile()))) {
            for (UUID uuid : favorites) {
                writer.println(uuid.toString());
            }
        } catch (IOException e) {
            System.err.println("[PCMod] Failed to save favorites: " + e.getMessage());
        }
    }

    public static boolean isFavorite(UUID uuid) {
        return favorites.contains(uuid);
    }

    public static void toggleFavorite(UUID uuid) {
        if (favorites.contains(uuid)) {
            favorites.remove(uuid);
        } else {
            favorites.add(uuid);
        }
        saveFavorites();
    }
}