package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class MultiGrabHandler {

    public static final int MAX_GRAB_SIZE = 30;

    private final List<Pokemon> selectedPokemon = new ArrayList<>();
    private final OverflowHandler overflowHandler = new OverflowHandler();

    public List<Pokemon> getSelectedPokemon() { return selectedPokemon; }

    // Gets the TRUE server position of the Pokemon for trashing/moving
    public List<StoragePosition> getSelectedTruePositions() {
        List<StoragePosition> positions = new ArrayList<>();
        for (Pokemon p : selectedPokemon) {
            if (p.getPosition() != null) positions.add(p.getPosition());
        }
        return positions;
    }

    public boolean isSelected(Pokemon pokemon) {
        if (pokemon == null) return false;
        // Check by exact UUID so it never loses track of the Pokemon!
        return selectedPokemon.stream().anyMatch(p -> p.getUUID().equals(pokemon.getUUID()));
    }

    public void toggleSelection(Pokemon pokemon) {
        if (isSelected(pokemon)) {
            selectedPokemon.removeIf(p -> p.getUUID().equals(pokemon.getUUID()));
        } else {
            if (selectedPokemon.size() < MAX_GRAB_SIZE) {
                selectedPokemon.add(pokemon);
                StoragePosition realPos = pokemon.getPosition();
                if (realPos != null) {
                    overflowHandler.recordOriginalPosition(pokemon, realPos);
                }
            }
        }
    }

    public int getSelectionCount() {
        return selectedPokemon.size();
    }

    public void clearSelection() {
        selectedPokemon.clear();
        overflowHandler.clearRecords();
    }

    public List<Pokemon> moveToBox(PCStorage pc, int targetBoxIndex) {
        PCBox targetBox = pc.getBox(targetBoxIndex);
        List<Pokemon> overflow = new ArrayList<>();

        if (targetBox == null) {
            overflow.addAll(selectedPokemon);
            overflowHandler.returnOverflow(pc, overflow);
            clearSelection();
            return overflow;
        }

        for (Pokemon pokemon : selectedPokemon) {
            StoragePosition realPos = pokemon.getPosition(); 
            int emptySlot = findEmptySlot(targetBox);

            if (emptySlot == -1) {
                overflow.add(pokemon);
            } else if (realPos != null) {
                PCBox originalBox = pc.getBox(realPos.box);
                if (originalBox != null) {
                    originalBox.set(realPos.order, null);
                }
                targetBox.set(emptySlot, pokemon);
            }
        }

        if (!overflow.isEmpty()) {
            overflowHandler.returnOverflow(pc, overflow);
        }

        clearSelection();
        return overflow;
    }

    private int findEmptySlot(PCBox box) {
        for (int i = 0; i < 30; i++) {
            if (box.get(i) == null) {
                return i;
            }
        }
        return -1;
    }
}