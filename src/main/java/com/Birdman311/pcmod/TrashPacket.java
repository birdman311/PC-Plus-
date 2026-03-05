package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TrashPacket {
    private final List<StoragePosition> positionsToDelete;

    public TrashPacket(List<StoragePosition> positionsToDelete) {
        this.positionsToDelete = positionsToDelete;
    }

    public static void encode(TrashPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.positionsToDelete.size());
        for (StoragePosition pos : msg.positionsToDelete) {
            buf.writeInt(pos.box);
            buf.writeInt(pos.order);
        }
    }

    public static TrashPacket decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<StoragePosition> positions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            positions.add(new StoragePosition(buf.readInt(), buf.readInt()));
        }
        return new TrashPacket(positions);
    }

    public static void handle(TrashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                PCStorage pc = StorageProxy.getPCForPlayer(player.getUUID());
                if (pc != null) {
                    for (StoragePosition pos : msg.positionsToDelete) {
                        PCBox box = pc.getBox(pos.box);
                        if (box != null) {
                            box.set(pos.order, null); // Permanently deletes the Pokemon
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}