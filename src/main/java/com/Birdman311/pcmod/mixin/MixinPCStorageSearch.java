package com.Birdman311.pcmod.mixin;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorageSearch;
import com.pixelmonmod.pixelmon.client.storage.ClientStorageManager;
import com.Birdman311.pcmod.SearchFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mixin(PCStorageSearch.class)
public abstract class MixinPCStorageSearch {

    @Shadow(remap = false) public int searchId;
    @Shadow(remap = false) public abstract void rearrangeBoxes(List<Pokemon> pokemon);

    @Inject(method = "query", at = @At("HEAD"), cancellable = true, remap = false)
    public void onQuery(String query, CallbackInfo ci) {
        if (query == null) return;

        String cleanQuery = query.replaceAll(" +", " ").toLowerCase(Locale.ROOT);

        if (SearchFilter.isValidFilter(cleanQuery)) {
            
            this.searchId++; 
            
            List<Pokemon> matches = new ArrayList<>();
            
            if (ClientStorageManager.openPC != null) {
                for (int b = 0; b < ClientStorageManager.openPC.getBoxCount(); b++) {
                    PCBox box = ClientStorageManager.openPC.getBox(b);
                    if (box != null) {
                        for (int i = 0; i < 30; i++) {
                            Pokemon p = box.get(i);
                            if (p != null && !p.isEgg() && SearchFilter.matchesFilter(cleanQuery, p)) {
                                matches.add(p);
                            }
                        }
                    }
                }
            }
            
            this.rearrangeBoxes(matches);
            ci.cancel();
        }
    }
}