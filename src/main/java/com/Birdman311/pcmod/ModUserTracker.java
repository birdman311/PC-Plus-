package com.Birdman311.pcmod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModUserTracker {

    private static final Set<UUID> moddedPlayers = ConcurrentHashMap.newKeySet();

    public static void setModUsers(Set<UUID> users) {
        moddedPlayers.clear();
        if (users != null) {
            moddedPlayers.addAll(users);
        }
    }

    public static boolean isModUser(UUID id) {
        return id != null && moddedPlayers.contains(id);
    }

    public static void clear() {
        moddedPlayers.clear();
    }
}
