package com.leclowndu93150.craftingrecipeexporter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
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
                Commands.literal("craftingrecipeexporter")
                        .executes(ModCommands::openMenu)
        );

        dispatcher.register(
                Commands.literal("cre")
                        .executes(ModCommands::openMenu)
        );

        dispatcher.register(
                Commands.literal("kubejs")
                        .then(Commands.literal("gui")
                                .executes(ModCommands::openMenu)
                        )
        );
    }

    private static int openMenu(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Crafting Recipe Exporter");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new CraftingJsonMenu(id, player);
                }
            });
        }
        return 1;
    }
}
