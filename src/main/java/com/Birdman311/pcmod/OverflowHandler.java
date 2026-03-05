package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OverflowHandler {

    private final Map<UUID, StoragePosition> originalPositions = new HashMap<>();

    public void recordOriginalPosition(Pokemon pokemon, StoragePosition position) {
        originalPositions.put(pokemon.getUUID(), position);
    }

    public void clearRecords() {
        originalPositions.clear();
    }

    public void returnOverflow(PCStorage pc, List<Pokemon> overflowList) {
        for (Pokemon pokemon : overflowList) {
            StoragePosition original = originalPositions.get(pokemon.getUUID());
            if (original != null) {
                PCBox box = pc.getBox(original.box);
                if (box != null) {
                    box.set(original.order, pokemon);
                }
            }
        }
        clearRecords();
    }
}