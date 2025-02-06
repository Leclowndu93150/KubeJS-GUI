package com.leclowndu93150.craftingrecipeexporter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingJsonScreen extends AbstractContainerScreen<CraftingJsonMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
    private Button exportJsonButton;
    private Button exportKubeJSButton;
    private Checkbox shapelessCheckbox;
    private Checkbox fixedPositionCheckbox;
    private boolean isShapeless = false;
    private boolean isFixed = false;

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
        int buttonWidth = 40;
        int spacing = 4;
        int startX = this.leftPos + 85;

        this.shapelessCheckbox = new Checkbox(
                startX+ 95, this.topPos + 30,
                50, 20,
                Component.literal("Shapeless"),
                this.isShapeless
        ) {
            @Override
            public void onPress() {
                super.onPress();
                isShapeless = this.selected();
            }
        };

        this.fixedPositionCheckbox = new Checkbox(
                startX + 55, this.topPos + 35,
                50, 20,
                Component.literal("Fixed"),
                this.isFixed
        ) {
            @Override
            public void onPress() {
                super.onPress();
                isFixed = this.selected();
            }
        };

        this.fixedPositionCheckbox.active = false;
        this.fixedPositionCheckbox.visible = false;

        this.exportJsonButton = Button.builder(Component.literal("JSON"), button -> {
                    if (Screen.hasControlDown()) {
                        String json = isShapeless ? generateShapelessJson() : generateJson();
                        Minecraft.getInstance().keyboardHandler.setClipboard(json);
                    } else {
                        if (isShapeless) {
                            exportShapelessRecipe();
                        } else {
                            exportRecipe();
                        }
                    }
                })
                .pos(startX, this.topPos + 58)
                .size(buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("Left click to export to crafting_recipes folder.\nHold Ctrl + click to copy to clipboard")))
                .build();

        this.exportKubeJSButton = Button.builder(Component.literal("KubeJS"), button -> {
                    if (Screen.hasControlDown()) {
                        String kubeJs = isShapeless ? generateShapelessKubeJS() : generateKubeJS();
                        Minecraft.getInstance().keyboardHandler.setClipboard(kubeJs);
                    } else {
                        if (isShapeless) {
                            exportShapelessKubeJSRecipe();
                        } else {
                            exportKubeJSRecipe();
                        }
                    }
                })
                .pos(startX + buttonWidth + spacing, this.topPos + 58)
                .size(buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("Left click to export to kubejs/server_scripts\nHold Ctrl + click to copy to clipboard")))
                .build();

        this.addRenderableWidget(shapelessCheckbox);
        this.addRenderableWidget(fixedPositionCheckbox);
        this.addRenderableWidget(exportJsonButton);
        this.addRenderableWidget(exportKubeJSButton);
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

    private void updateCheckboxStates() {
        this.isShapeless = this.shapelessCheckbox.selected();
        this.isFixed = this.fixedPositionCheckbox.selected();
    }

    private void exportShapelessRecipe() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i < 10; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");

        JsonArray ingredients = new JsonArray();
        for (ItemStack input : inputs) {
            ingredients.add(serializeItem(input));
        }

        json.add("ingredients", ingredients);
        json.add("result", serializeResult(output));

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

        try {
            Path modsDir = FMLPaths.GAMEDIR.get().resolve("crafting_recipes");
            Files.createDirectories(modsDir);
            Path recipePath = modsDir.resolve("shapeless_recipe_" + System.currentTimeMillis() + ".json");
            Files.writeString(recipePath, jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateShapelessJson() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i < 10; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");

        JsonArray ingredients = new JsonArray();
        for (ItemStack input : inputs) {
            ingredients.add(serializeItem(input));
        }

        json.add("ingredients", ingredients);
        json.add("result", serializeResult(output));

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private void exportShapelessKubeJSRecipe() {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("kubejs/server_scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve("exported_recipes.js");

            String newRecipe = generateShapelessKubeJS();
            String existingContent = "";

            if (Files.exists(recipePath)) {
                existingContent = Files.readString(recipePath);
            }

            if (!existingContent.contains("ServerEvents.recipes")) {
                existingContent = "ServerEvents.recipes(event => {\n\n});\n";
            }

            if (!existingContent.contains(newRecipe.trim())) {
                String updatedContent = existingContent.replace("});", newRecipe + "\n});");
                Files.writeString(recipePath, updatedContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateShapelessKubeJS() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i < 10; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        StringBuilder script = new StringBuilder();

        if (!Screen.hasControlDown()) {
            script.append("    ");
        }

        script.append("event.shapeless(\n");
        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    Item.of('").append(ForgeRegistries.ITEMS.getKey(output.getItem())).append("'");
        if (output.getCount() > 1) {
            script.append(", ").append(output.getCount());
        }
        script.append("),\n");

        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    [\n");

        for (ItemStack input : inputs) {
            if (!Screen.hasControlDown()) {
                script.append("            ");
            }
            script.append("        '").append(ForgeRegistries.ITEMS.getKey(input.getItem())).append("'");
            if (input.getCount() > 1) {
                script.append(", ").append(input.getCount());
            }
            if (input != inputs.get(inputs.size() - 1)) { // Only add comma if not last item
                script.append(",");
            }
            script.append("\n");
        }

        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    ]\n");
        if (!Screen.hasControlDown()) {
            script.append("    ");
        }
        script.append(")");

        return script.toString();
    }

    private void exportRecipe() {
        updateCheckboxStates();
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

        if (isFixed) {
            JsonObject conditions = new JsonObject();
            conditions.addProperty("fixed", true);
            json.add("conditions", conditions);
        }

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

    private String generateJson() {
        updateCheckboxStates();
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

        if (isFixed) {
            JsonObject conditions = new JsonObject();
            conditions.addProperty("fixed", true);
            json.add("conditions", conditions);
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private void exportKubeJSRecipe() {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("kubejs/server_scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve("exported_recipes.js");

            String newRecipe = generateKubeJS();
            String existingContent = "";

            if (Files.exists(recipePath)) {
                existingContent = Files.readString(recipePath);
            }

            if (!existingContent.contains("ServerEvents.recipes")) {
                existingContent = "ServerEvents.recipes(event => {\n\n});\n";
            }

            if (!existingContent.contains(newRecipe.trim())) {
                String updatedContent = existingContent.replace("});", newRecipe + "\n});");
                Files.writeString(recipePath, updatedContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateKubeJS() {
        updateCheckboxStates();
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i < 10; i++) {
            inputs.add(this.menu.getSlot(i).getItem());
        }

        StringBuilder script = new StringBuilder();

        if (!Screen.hasControlDown()) {
            script.append("    ");
        }

        script.append("event.shaped(\n");
        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    Item.of('").append(ForgeRegistries.ITEMS.getKey(output.getItem())).append("'");
        if (output.getCount() > 1) {
            script.append(", ").append(output.getCount());
        }
        script.append("),\n");

        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    [\n");

        char currentKey = 'A';
        Map<String, Character> itemToKey = new HashMap<>();

        boolean hasItems = false;
        for (int row = 0; row < 3; row++) {
            if (!Screen.hasControlDown()) {
                script.append("            ");
            }
            script.append("        '");
            for (int col = 0; col < 3; col++) {
                ItemStack stack = inputs.get(row * 3 + col);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId != null) {
                        String itemPath = itemId.toString();
                        if (!itemToKey.containsKey(itemPath)) {
                            itemToKey.put(itemPath, currentKey++);
                        }
                        script.append(itemToKey.get(itemPath));
                        hasItems = true;
                    } else {
                        script.append(" ");
                    }
                } else {
                    script.append(" ");
                }
            }
            if (hasItems) {
                script.append(row < 2 ? "',\n" : "'\n");
            } else if (row < 2) {
                script.append("',\n");
            } else {
                script.append("'\n");
            }
        }
        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    ],\n");

        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    {\n");
        boolean first = true;
        for (Map.Entry<String, Character> entry : itemToKey.entrySet()) {
            if (!first) script.append(",\n");
            if (!Screen.hasControlDown()) {
                script.append("            ");
            }
            script.append("        ").append(entry.getValue()).append(": '").append(entry.getKey()).append("'");
            first = false;
        }
        script.append("\n");
        if (!Screen.hasControlDown()) {
            script.append("        ");
        }
        script.append("    }");

        if (isFixed) {
            script.append(",\n");
            if (!Screen.hasControlDown()) {
                script.append("        ");
            }
            script.append("    { fixed: true }");
        }

        script.append("\n");
        if (!Screen.hasControlDown()) {
            script.append("    ");
        }
        script.append(")");

        return script.toString();
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