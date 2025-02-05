package com.leclowndu93150.craftingjsonmod;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CraftingJsonScreen extends AbstractContainerScreen<CraftingJsonMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
    private Button exportButton;

    public CraftingJsonScreen(CraftingJsonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.minecraft = Minecraft.getInstance();
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.exportButton = Button.builder(Component.literal("Export JSON"), button -> exportRecipe())
                .pos(this.leftPos + 124, this.topPos + 58)  // Positioned under output slot
                .size(60, 20)
                .build();
        this.addRenderableWidget(exportButton);
        this.titleLabelX = 28;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int mouseButton) {
        return super.hasClickedOutside(mouseX, mouseY, leftPos, topPos, mouseButton);
    }


    private void exportRecipe() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i < 10; i++) {
            inputs.add(this.menu.getSlot(i).getItem());
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");

        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        char currentKey = 'A';

        for (int row = 0; row < 3; row++) {
            StringBuilder patternRow = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                ItemStack stack = inputs.get(row * 3 + col);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId != null) {
                        key.add(String.valueOf(currentKey), serializeItem(stack));
                        patternRow.append(currentKey++);
                    }
                } else {
                    patternRow.append(" ");
                }
            }
            pattern.add(patternRow.toString());
        }

        json.add("pattern", pattern);
        json.add("key", key);
        json.add("result", serializeResult(output));

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

        try {
            Path modsDir = FMLPaths.GAMEDIR.get().resolve("crafting_recipes");
            Files.createDirectories(modsDir);
            Path recipePath = modsDir.resolve("recipe_" + System.currentTimeMillis() + ".json");
            Files.writeString(recipePath, jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject serializeItem(ItemStack stack) {
        JsonObject item = new JsonObject();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            item.addProperty("item", itemId.toString());
            if (stack.getCount() > 1) {
                item.addProperty("count", stack.getCount());
            }
        }
        return item;
    }

    private JsonObject serializeResult(ItemStack stack) {
        JsonObject result = new JsonObject();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            result.addProperty("item", itemId.toString());
            result.addProperty("count", stack.getCount());
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}