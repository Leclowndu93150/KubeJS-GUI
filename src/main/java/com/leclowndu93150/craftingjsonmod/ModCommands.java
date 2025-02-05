package com.leclowndu93150.craftingjsonmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("craftingtable")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof Player player) {
                                player.openMenu(new MenuProvider() {
                                    @Override
                                    public Component getDisplayName() {
                                        return Component.literal("Crafting JSON Export");
                                    }

                                    @Override
                                    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                                        return new CraftingJsonMenu(id, player);
                                    }
                                });
                            }
                            return 1;
                        })
        );
    }
}
