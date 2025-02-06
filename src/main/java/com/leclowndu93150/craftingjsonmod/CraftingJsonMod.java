package com.leclowndu93150.craftingjsonmod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CraftingJsonMod.MODID)
public class CraftingJsonMod {
    public static final String MODID = "craftingjson";

    public CraftingJsonMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CraftingJsonMenu.MENUS.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(CraftingJsonMenu.TYPE.get(), CraftingJsonScreen::new);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}