package com.leclowndu93150.craftingjsonmod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CraftingJsonMenu extends RecipeBookMenu<CraftingContainer> {
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots;
    private final Player player;
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CraftingJsonMod.MODID);
    public static final RegistryObject<MenuType<CraftingJsonMenu>> TYPE = MENUS.register("crafting_json",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CraftingJsonMenu(windowId, inv.player)));

    public CraftingJsonMenu(int id, Player player) {
        super(TYPE.get(), id);
        this.craftSlots = new TransientCraftingContainer(this, 3, 3);
        this.resultSlots = new ResultContainer();
        this.player = player;

        this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, 124, 35){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for(int k = 0; k < 3; k++) {
            for(int i1 = 0; i1 < 9; i1++) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for(int l = 0; l < 9; l++) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.craftSlots);
        this.clearContainer(player, this.resultSlots);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 0) { // Result slot
                if (!this.moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 10 && index < 37) { // Main inventory
                if (!this.moveItemStackTo(slotStack, 37, 46, false) &&
                        !this.moveItemStackTo(slotStack, 1, 10, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 37 && index < 46) { // Hotbar
                if (!this.moveItemStackTo(slotStack, 10, 37, false) &&
                        !this.moveItemStackTo(slotStack, 1, 10, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 10, 46, false)) { // Crafting slots
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
            broadcastChanges();
        }
        return itemstack;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot != this.getResultSlotIndex();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        this.craftSlots.fillStackedContents(contents);
    }

    @Override
    public void clearCraftingContent() {

    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftSlots, this.player.level());
    }

    @Override
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    @Override
    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    @Override
    public int getSize() {
        return 10;
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }
}