package com.Birdman311.pcmod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;

@Mod("pcmod")
public class PCMod {

    public static final String MOD_ID = "pcmod";
    private static PCEventHandler eventHandler;

    public static final String NETWORK_VERSION = "1.0";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MOD_ID, "network"),
        () -> NETWORK_VERSION,
        NETWORK_VERSION::equals,
        NETWORK_VERSION::equals
    );

    public PCMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        eventHandler = new PCEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        MinecraftForge.EVENT_BUS.register(new PCModGuiOverlay());
        MinecraftForge.EVENT_BUS.register(new PCClickHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        int id = 0;
        NETWORK.registerMessage(id++, TrashPacket.class, TrashPacket::encode, TrashPacket::decode, TrashPacket::handle);
        System.out.println("[PCMod] PC Plus+ loaded successfully!");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        KeyInit.register();
    }

    public static PCEventHandler getEventHandler() {
        return eventHandler;
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        FavoriteManager.loadFavorites();

        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            PresenceClient.start(PresenceClient.resolveServerKey());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        FavoriteManager.saveFavorites();
        PresenceClient.stop();
    }
}
