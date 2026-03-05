package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.events.storage.ChangeStorageEvent;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PCEventHandler {

    private final Map<UUID, MultiGrabHandler> playerHandlers = new HashMap<>();

    public MultiGrabHandler getHandler(PlayerEntity player) {
        return playerHandlers.computeIfAbsent(
            player.getUUID(), id -> new MultiGrabHandler()
        );
    }

    public MultiGrabHandler getHandlerForClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return getHandler(mc.player);
    }

    @SubscribeEvent
    public void onStorageChange(ChangeStorageEvent event) {
        if (event.pokemon != null) {
            UUID ownerUUID = event.pokemon.getOwnerPlayerUUID();
            if (ownerUUID != null && playerHandlers.containsKey(ownerUUID)) {
                // Maintained natural clearing behavior
            }
        }
    }

    public void onMultiSelectClick(PlayerEntity player, Pokemon pokemon) {
        MultiGrabHandler handler = getHandler(player);
        handler.toggleSelection(pokemon);

        System.out.println("[PCMod] " + player.getName().getString()
            + " selected " + handler.getSelectionCount() + " Pokemon");
    }

    public void onConfirmMove(PlayerEntity player, int targetBox) {
        MultiGrabHandler handler = getHandler(player);
        if (handler.getSelectionCount() == 0) return;

        PCStorage pc = StorageProxy.getPCForPlayer(player.getUUID());
        if (pc == null) return;

        List<Pokemon> overflow = handler.moveToBox(pc, targetBox);

        if (!overflow.isEmpty()) {
            System.out.println("[PCMod] " + overflow.size()
                + " Pokemon returned to original slots due to full box.");
        }
    }

    public void clearPlayer(UUID playerUUID) {
        playerHandlers.remove(playerUUID);
    }
}