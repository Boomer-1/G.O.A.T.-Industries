package com.boomer1.goatindustries;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public final class DynamicCrystalFormer {

    private static final int[] OUTPUT_SLOTS = {24, 25};
    private static final int[] INPUT_MARKERS = {0, 1, 2, 3, 4, 9, 13, 18, 27, 31, 36, 37, 38, 39, 40};
    private static final int[] OUTPUT_MARKERS = {14, 15, 16, 17, 23, 26, 32, 33, 34, 35};

    private DynamicCrystalFormer() {
    }

    public static void register(
        JavaPlugin plugin,
                ItemGroup machineGroup,
        ItemGroup resourceGroup,
        SlimefunAddon addon
    ) throws java.net.MalformedURLException {

        SlimefunItemStack machineStack = new SlimefunItemStack(
            DynamicCrystalCatalog.MACHINE_ID,
            Material.valueOf(DynamicCrystalCatalog.MACHINE_MATERIAL),
            ChatColor.GOLD + DynamicCrystalCatalog.MACHINE_NAME,
            ChatColor.GRAY + DynamicCrystalCatalog.MACHINE_LORE,
            "",
            ChatColor.DARK_PURPLE + "End-Game Machine",
            ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
            ChatColor.GRAY + "Energy Buffer: 16,000,000 J"
        );

        ItemStack[] machineCraftingRecipe = resolveItems(DynamicCrystalCatalog.MACHINE_RECIPE_IDS);
        DynamicCrystalFormerMachine machine = new DynamicCrystalFormerMachine(
            machineGroup,
            machineStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            machineCraftingRecipe
        );
        machine.setCapacity(DynamicCrystalCatalog.ENERGY_CAPACITY);
        machine.setEnergyConsumption(DynamicCrystalCatalog.ENERGY_PER_TICK);
        machine.setProcessingSpeed(1);

        RecipeType dynamicCrystalRecipeType = new RecipeType(
            new NamespacedKey(plugin, "dynamic_crystal_former"),
            machineStack
        );

        machine.register(addon);

        for (DynamicCrystalCatalog.CrystalOutput output : DynamicCrystalCatalog.OUTPUTS) {
            SlimefunItemStack outputStack = new SlimefunItemStack(
                output.id(),
                Material.PLAYER_HEAD,
                ChatColor.AQUA + output.displayName(),
                ChatColor.GRAY + DynamicCrystalCatalog.DYNAMIC_CRYSTAL_LORE
            );
            setHeadTexture(outputStack, output.textureUrl());

            ItemMeta outputMeta = outputStack.getItemMeta();
            outputMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "dynamic_crystal"),
                PersistentDataType.BYTE,
                (byte) 1
            );
            outputStack.setItemMeta(outputMeta);

            ItemStack[] inputs = resolveItems(DynamicCrystalCatalog.inputIds(output.rarity()));
            new UnplaceableBlock(resourceGroup, outputStack, dynamicCrystalRecipeType, inputs).register(addon);

            machine.registerRecipe(
                DynamicCrystalCatalog.PROCESSING_SECONDS,
                inputs,
                new ItemStack[] {outputStack.clone()}
            );
        }

        // The Omega Supercrystal uses the six completed Dynamic Crystals in the specified 3x3 layout.
        SlimefunItemStack omegaStack = new SlimefunItemStack(
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_NAME,
            ChatColor.GRAY + DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_LORE
        );
        setHeadTexture(omegaStack, DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_TEXTURE);

        ItemMeta omegaMeta = omegaStack.getItemMeta();
        omegaMeta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "dynamic_crystal"),
            PersistentDataType.BYTE,
            (byte) 1
        );
        omegaStack.setItemMeta(omegaMeta);

        ItemStack[] omegaInputs = new ItemStack[9];
        int[] omegaIngredientPositions = {0, 1, 2, 6, 7, 8};
        ItemStack[] omegaIngredients = resolveItems(DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_INPUT_IDS);
        for (int i = 0; i < omegaIngredientPositions.length; i++) {
            omegaIngredients[i].setAmount(9);
            omegaInputs[omegaIngredientPositions[i]] = omegaIngredients[i];
        }
        // The center row is intentionally empty; AIR entries preserve the exact 3x3 recipe shape.
        for (int i = 3; i <= 5; i++) {
            omegaInputs[i] = new ItemStack(Material.AIR);
        }

        new UnplaceableBlock(resourceGroup, omegaStack, dynamicCrystalRecipeType, omegaInputs).register(addon);
        machine.registerRecipe(
            DynamicCrystalCatalog.PROCESSING_SECONDS,
            omegaInputs,
            new ItemStack[] {omegaStack.clone()}
        );
    }

    private static ItemStack[] resolveItems(List<String> ids) {
        ItemStack[] items = new ItemStack[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            SlimefunItem item = SlimefunItem.getById(ids.get(i));
            if (item == null) {
                throw new IllegalStateException("Required Slimefun item is not registered: " + ids.get(i));
            }
            items[i] = item.getItem().clone();
        }
        return items;
    }

    private static void setHeadTexture(ItemStack stack, String textureUrl) throws java.net.MalformedURLException {
        ItemMeta itemMeta = stack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta skullMeta)) {
            throw new IllegalStateException("The Dynamic Crystal output is not a player head.");
        }

        UUID profileId = UUID.nameUUIDFromBytes(
            (stack.getType().name() + ":" + itemMeta.getDisplayName()).getBytes(StandardCharsets.UTF_8)
        );
        PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "DynamicCrystal");
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(new URL(textureUrl));
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
        stack.setItemMeta(skullMeta);
    }

    private static ItemStack createProgressItem() {
        ItemStack progress = new ItemStack(Material.CLOCK);
        ItemMeta meta = progress.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Processing");
        progress.setItemMeta(meta);
        return progress;
    }

    private static boolean contains(int[] slots, int target) {
        for (int slot : slots) {
            if (slot == target) {
                return true;
            }
        }
        return false;
    }

    private static final class DynamicCrystalFormerMachine extends AContainer {

        private static final ItemStack PROGRESS_BAR = createProgressItem();

        private DynamicCrystalFormerMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe
        ) {
            super(itemGroup, item, recipeType, recipe);
        }

        @Override
        public ItemStack getProgressBar() {
            return PROGRESS_BAR.clone();
        }

        @Override
        public String getMachineIdentifier() {
            return DynamicCrystalCatalog.MACHINE_ID;
        }

        @Override
        public int[] getInputSlots() {
            return DynamicCrystalCatalog.inputSlots();
        }

        @Override
        public int[] getOutputSlots() {
            return OUTPUT_SLOTS.clone();
        }

        @Override
        protected MachineRecipe findNextRecipe(BlockMenu menu) {
            int[] inputSlots = DynamicCrystalCatalog.inputSlots();

            for (MachineRecipe recipe : getMachineRecipes()) {
                ItemStack[] requiredInputs = recipe.getInput();
                if (requiredInputs.length != inputSlots.length) {
                    continue;
                }

                boolean matches = true;
                for (int i = 0; i < inputSlots.length; i++) {
                    ItemStack actual = menu.getItemInSlot(inputSlots[i]);
                    ItemStack expected = requiredInputs[i];

                    if (expected == null || expected.getType() == Material.AIR) {
                        if (actual != null && actual.getType() != Material.AIR) {
                            matches = false;
                            break;
                        }
                        continue;
                    }

                    if (actual == null
                        || actual.getType() == Material.AIR
                        || actual.getAmount() < expected.getAmount()
                        || !sameSlimefunItem(actual, expected)) {
                        matches = false;
                        break;
                    }
                }

                if (!matches || !canFitOutput(menu, recipe.getOutput())) {
                    continue;
                }

                for (int i = 0; i < inputSlots.length; i++) {
                    ItemStack requiredInput = requiredInputs[i];
                    if (requiredInput != null && requiredInput.getType() != Material.AIR) {
                        menu.consumeItem(inputSlots[i], requiredInput.getAmount());
                    }
                }
                return recipe;
            }

            return null;
        }

        private boolean sameSlimefunItem(ItemStack first, ItemStack second) {
            SlimefunItem firstItem = SlimefunItem.getByItem(first);
            SlimefunItem secondItem = SlimefunItem.getByItem(second);
            return firstItem != null
                && secondItem != null
                && firstItem.getId().equals(secondItem.getId());
        }

        private boolean canFitOutput(BlockMenu menu, ItemStack[] outputs) {
            int availableCapacity = 0;
            ItemStack outputTexture = ChestMenuUtils.getOutputSlotTexture();

            for (int slot : OUTPUT_SLOTS) {
                ItemStack current = menu.getItemInSlot(slot);
                if (current == null || current.getType() == Material.AIR || current.isSimilar(outputTexture)) {
                    for (ItemStack output : outputs) {
                        availableCapacity += output.getMaxStackSize();
                    }
                } else {
                    boolean matchesOutput = false;
                    for (ItemStack output : outputs) {
                        if (sameSlimefunItem(current, output)) {
                            availableCapacity += output.getMaxStackSize() - current.getAmount();
                            matchesOutput = true;
                            break;
                        }
                    }
                    if (!matchesOutput) {
                        return false;
                    }
                }
            }

            int requiredAmount = 0;
            for (ItemStack output : outputs) {
                requiredAmount += output.getAmount();
            }
            return availableCapacity >= requiredAmount;
        }

        @Override
        protected void constructMenu(BlockMenuPreset preset) {
            ItemStack background = ChestMenuUtils.getBackground();
            ItemStack inputTexture = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
            ItemMeta inputMeta = inputTexture.getItemMeta();
            inputMeta.setDisplayName(ChatColor.DARK_BLUE + "Crystal Inputs");
            inputTexture.setItemMeta(inputMeta);

            ItemStack outputTexture = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
            ItemMeta outputMeta = outputTexture.getItemMeta();
            outputMeta.setDisplayName(ChatColor.GOLD + "Crystal Output");
            outputTexture.setItemMeta(outputMeta);

            for (int slot = 0; slot < 54; slot++) {
                boolean isInputSlot = contains(DynamicCrystalCatalog.inputSlots(), slot);
                boolean isInputMarker = contains(INPUT_MARKERS, slot);
                boolean isOutputMarker = contains(OUTPUT_MARKERS, slot);
                boolean isOpenOutput = slot == OUTPUT_SLOTS[0] || slot == OUTPUT_SLOTS[1];

                // Keep every ingredient and product slot genuinely empty and usable.
                if (!isInputSlot && !isInputMarker && !isOutputMarker && !isOpenOutput
                    && slot != DynamicCrystalCatalog.PROGRESS_SLOT && slot != 5) {
                    preset.addItem(slot, background.clone(), ChestMenuUtils.getEmptyClickHandler());
                }
            }

            // Colored panes identify the input/output areas, but never occupy usable slots.
            for (int slot : INPUT_MARKERS) {
                preset.addItem(slot, inputTexture.clone(), ChestMenuUtils.getEmptyClickHandler());
            }
            for (int slot : OUTPUT_MARKERS) {
                preset.addItem(slot, outputTexture.clone(), ChestMenuUtils.getEmptyClickHandler());
            }

            // Prevent inserting items into output slots while allowing players to collect products.
            for (int slot : OUTPUT_SLOTS) {
                preset.addMenuClickHandler(slot, new AdvancedMenuClickHandler() {
                    @Override
                    public boolean onClick(Player player, int clickedSlot, ItemStack cursor, ClickAction action) {
                        return false;
                    }

                    @Override
                    public boolean onClick(
                        InventoryClickEvent event,
                        Player player,
                        int clickedSlot,
                        ItemStack cursor,
                        ClickAction action
                    ) {
                        return cursor == null || cursor.getType() == null || cursor.getType() == Material.AIR;
                    }
                });
            }

            ItemStack progress = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta progressMeta = progress.getItemMeta();
            progressMeta.setDisplayName(" ");
            progress.setItemMeta(progressMeta);
            preset.addItem(
                DynamicCrystalCatalog.PROGRESS_SLOT,
                progress,
                ChestMenuUtils.getEmptyClickHandler()
            );

            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta = arrow.getItemMeta();
            arrowMeta.setDisplayName(ChatColor.GOLD + "Output");
            arrow.setItemMeta(arrowMeta);
            preset.addItem(5, arrow, ChestMenuUtils.getEmptyClickHandler());
        }
    }
}
