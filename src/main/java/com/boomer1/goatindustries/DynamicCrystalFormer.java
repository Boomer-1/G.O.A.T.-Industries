package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicCrystalFormer {

    private static final String ITEM_GROUP_CLASS = "io.github.thebusybiscuit.slimefun4.api.items.ItemGroup";
    private static final String ITEM_STACK_CLASS = "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack";
    private static final String SLIMEFUN_ITEM_CLASS = "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem";
    private static final String RECIPE_TYPE_CLASS = "io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType";
    private static final String NOT_PLACEABLE_CLASS = "io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable";
    private static final String A_CONTAINER_CLASS =
        "me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer";
    private static final String MACHINE_RECIPE_CLASS =
        "me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe";
    private static final String CHEST_MENU_UTILS_CLASS = "io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils";
    private static final String MENU_CLICK_HANDLER_CLASS =
        "me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu$MenuClickHandler";
    private static final String ADVANCED_MENU_CLICK_HANDLER_CLASS =
        "me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu$AdvancedMenuClickHandler";

    private DynamicCrystalFormer() {
    }

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object itemGroup, Object addon)
        throws ReflectiveOperationException, java.io.IOException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);

        ItemStack machineStack = createSlimefunStack(
            slimefunLoader,
            DynamicCrystalCatalog.MACHINE_ID,
            Material.valueOf(DynamicCrystalCatalog.MACHINE_MATERIAL),
            ChatColor.GOLD + DynamicCrystalCatalog.MACHINE_NAME,
            new String[] {
                ChatColor.GRAY + DynamicCrystalCatalog.MACHINE_LORE,
                "",
                ChatColor.DARK_PURPLE + "End-Game Machine",
                ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
                ChatColor.GRAY + "Energy Buffer: 16,000,000 J"
            }
        );

        ItemStack[] machineCraftingRecipe = resolveItems(slimefunLoader, DynamicCrystalCatalog.MACHINE_RECIPE_IDS);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        ItemStack progressBar = new ItemStack(Material.CLOCK);
        ItemMeta progressMeta = progressBar.getItemMeta();
        progressMeta.setDisplayName(ChatColor.YELLOW + "Processing");
        progressBar.setItemMeta(progressMeta);

        Class<?> machineClass = createMachineClass(plugin.getClass().getClassLoader(), slimefunLoader, progressBar);
        Constructor<?> machineConstructor = machineClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object machine = machineConstructor.newInstance(itemGroup, machineStack, enhancedCraftingTable, machineCraftingRecipe);

        machineClass.getMethod("setCapacity", int.class).invoke(machine, DynamicCrystalCatalog.ENERGY_CAPACITY);
        machineClass.getMethod("setEnergyConsumption", int.class).invoke(machine, DynamicCrystalCatalog.ENERGY_PER_TICK);
        machineClass.getMethod("setProcessingSpeed", int.class).invoke(machine, 1);

        Method registerMachineRecipe = machineClass.getMethod("registerRecipe", int.class, ItemStack[].class, ItemStack[].class);
        Constructor<?> recipeTypeConstructor = recipeTypeClass.getConstructor(NamespacedKey.class, ItemStack.class);
        Object dynamicCrystalRecipeType = recipeTypeConstructor.newInstance(
            new NamespacedKey(plugin, "dynamic_crystal_former"), machineStack
        );

        Class<?> outputItemClass = createNotPlaceableItemClass(plugin.getClass().getClassLoader(), slimefunLoader);
        Constructor<?> outputItemConstructor = outputItemClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Method registerItem = slimefunItemClass.getMethod(
            "register",
            Class.forName("io.github.thebusybiscuit.slimefun4.api.SlimefunAddon", true, slimefunLoader)
        );

        registerItem.invoke(machine, addon);

        for (DynamicCrystalCatalog.CrystalOutput output : DynamicCrystalCatalog.OUTPUTS) {
            ItemStack outputStack = createSlimefunStack(
                slimefunLoader,
                output.id(),
                Material.PLAYER_HEAD,
                ChatColor.AQUA + output.displayName(),
                new String[] {
                    ChatColor.GRAY + DynamicCrystalCatalog.DYNAMIC_CRYSTAL_LORE
                }
            );
            setHeadTexture(outputStack, output.textureUrl(), slimefunLoader);

            ItemMeta outputMeta = outputStack.getItemMeta();
            outputMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "dynamic_crystal"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1
            );
            outputStack.setItemMeta(outputMeta);

            ItemStack[] inputs = resolveItems(slimefunLoader, DynamicCrystalCatalog.inputIds(output.rarity()));
            Object outputItem = outputItemConstructor.newInstance(itemGroup, outputStack, dynamicCrystalRecipeType, inputs);
            registerItem.invoke(outputItem, addon);

            registerMachineRecipe.invoke(
                machine,
                DynamicCrystalCatalog.PROCESSING_SECONDS,
                inputs,
                new ItemStack[] {outputStack.clone()}
            );
        }

        // The Omega Supercrystal uses the six completed Dynamic Crystals in the specified 3x3 layout.
        ItemStack omegaStack = createSlimefunStack(
            slimefunLoader,
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_NAME,
            new String[] {
                ChatColor.GRAY + DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_LORE
            }
        );
        setHeadTexture(omegaStack, DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_TEXTURE, slimefunLoader);

        ItemMeta omegaMeta = omegaStack.getItemMeta();
        omegaMeta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "dynamic_crystal"),
            org.bukkit.persistence.PersistentDataType.BYTE,
            (byte) 1
        );
        omegaStack.setItemMeta(omegaMeta);

        ItemStack[] omegaInputs = new ItemStack[9];
        int[] omegaIngredientPositions = {0, 1, 2, 6, 7, 8};
        ItemStack[] omegaIngredients = resolveItems(
            slimefunLoader,
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_INPUT_IDS
        );
        for (int i = 0; i < omegaIngredientPositions.length; i++) {
            omegaInputs[omegaIngredientPositions[i]] = omegaIngredients[i];
        }
        // The center row is intentionally empty; AIR entries preserve the exact 3x3 recipe shape.
        for (int i = 3; i <= 5; i++) {
            omegaInputs[i] = new ItemStack(Material.AIR);
        }

        Object omegaItem = outputItemConstructor.newInstance(
            itemGroup, omegaStack, dynamicCrystalRecipeType, omegaInputs
        );
        registerItem.invoke(omegaItem, addon);
        registerMachineRecipe.invoke(
            machine,
            DynamicCrystalCatalog.PROCESSING_SECONDS,
            omegaInputs,
            new ItemStack[] {omegaStack.clone()}
        );

        plugin.getLogger().info("[G.O.A.T. Industries] Dynamic Crystal Former registered with six exact 3x3 crystal recipes and the Omega Supercrystal recipe.");
    }

    private static Class<?> createMachineClass(
        ClassLoader pluginLoader, ClassLoader slimefunLoader, ItemStack progressBar
    ) throws ReflectiveOperationException {

        Class<?> aContainerClass = Class.forName(A_CONTAINER_CLASS, true, slimefunLoader);
        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Constructor<?> superConstructor = aContainerClass.getDeclaredConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );

        return new ByteBuddy()
            .subclass(aContainerClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .name("com.boomer1.goatindustries.generated.DynamicCrystalFormerMachine")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .method(ElementMatchers.named("getProgressBar").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(progressBar))
            .method(ElementMatchers.named("getMachineIdentifier").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(DynamicCrystalCatalog.MACHINE_ID))
            .method(ElementMatchers.named("getInputSlots").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(DynamicCrystalCatalog.inputSlots()))
            .method(ElementMatchers.named("getOutputSlots").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(new int[] {24, 25}))
            .method(ElementMatchers.named("findNextRecipe").and(ElementMatchers.takesArguments(1)))
            .intercept(MethodDelegation.to(new ExactRecipeMatcher()))
            .method(ElementMatchers.named("constructMenu").and(ElementMatchers.takesArguments(1)))
            .intercept(MethodDelegation.to(new MachineMenuConfigurator()))
            .make()
            .load(pluginLoader, ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }

    private static Class<?> createNotPlaceableItemClass(ClassLoader pluginLoader, ClassLoader slimefunLoader)
        throws ReflectiveOperationException {

        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> notPlaceableClass = Class.forName(NOT_PLACEABLE_CLASS, true, slimefunLoader);
        Constructor<?> superConstructor = slimefunItemClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );

        return new ByteBuddy()
            .subclass(slimefunItemClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .implement(notPlaceableClass)
            .name("com.boomer1.goatindustries.generated.NotPlaceableDynamicCrystal")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .make()
            .load(pluginLoader, ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }

    private static ItemStack createSlimefunStack(
        ClassLoader loader, String id, Material material, String displayName, String[] lore
    ) throws ReflectiveOperationException {
        Class<?> stackClass = Class.forName(ITEM_STACK_CLASS, true, loader);
        Constructor<?> constructor = stackClass.getConstructor(String.class, Material.class, String.class, String[].class);
        return (ItemStack) constructor.newInstance(id, material, displayName, lore);
    }

    private static ItemStack[] resolveItems(ClassLoader loader, List<String> ids) throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
        Method getById = slimefunItemClass.getMethod("getById", String.class);
        Method getItem = slimefunItemClass.getMethod("getItem");
        ItemStack[] items = new ItemStack[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            Object slimefunItem = getById.invoke(null, ids.get(i));
            if (slimefunItem == null) {
                throw new IllegalStateException("Required Slimefun item is not registered: " + ids.get(i));
            }
            items[i] = ((ItemStack) getItem.invoke(slimefunItem)).clone();
        }
        return items;
    }

    private static void setHeadTexture(ItemStack stack, String textureUrl, ClassLoader loader)
        throws ReflectiveOperationException, java.net.MalformedURLException {

        ItemMeta meta = stack.getItemMeta();
        Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta", true, loader);
        if (!skullMetaClass.isInstance(meta)) {
            throw new IllegalStateException("The Dynamic Crystal output is not a player head.");
        }

        Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile", true, loader);
        Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures", true, loader);
        UUID profileId = UUID.nameUUIDFromBytes(
            (stack.getType().name() + ":" + meta.getDisplayName()).getBytes(StandardCharsets.UTF_8)
        );

        Method createProfile;
        Object profile;
        try {
            createProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);
            profile = createProfile.invoke(null, profileId, "DynamicCrystal");
        } catch (NoSuchMethodException exception) {
            createProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class);
            profile = createProfile.invoke(null, profileId);
        }

        Object textures = profileClass.getMethod("getTextures").invoke(profile);
        texturesClass.getMethod("setSkin", URL.class).invoke(textures, new URL(textureUrl));
        profileClass.getMethod("setTextures", texturesClass).invoke(profile, textures);

        Method applyProfile;
        try {
            applyProfile = skullMetaClass.getMethod("setOwnerProfile", profileClass);
        } catch (NoSuchMethodException exception) {
            applyProfile = skullMetaClass.getMethod("setPlayerProfile", profileClass);
        }
        applyProfile.invoke(meta, profile);
        stack.setItemMeta(meta);
    }

    public static final class ExactRecipeMatcher {

        @RuntimeType
        public Object findNextRecipe(@This Object machine, @Argument(0) Object menu) {
            try {
                ClassLoader loader = machine.getClass().getClassLoader();
                Class<?> machineRecipeClass = Class.forName(MACHINE_RECIPE_CLASS, true, loader);
                Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
                Class<?> blockMenuClass = Class.forName(
                    "me.mrCookieSlime.Slimefun.api.inventory.BlockMenu", true, loader
                );

                Method getMachineRecipes = machine.getClass().getMethod("getMachineRecipes");
                Method getInput = machineRecipeClass.getMethod("getInput");
                Method getOutput = machineRecipeClass.getMethod("getOutput");
                Method getByItem = slimefunItemClass.getMethod("getByItem", ItemStack.class);
                Method getId = slimefunItemClass.getMethod("getId");
                Method getItemInSlot = blockMenuClass.getMethod("getItemInSlot", int.class);
                Method consumeItem = blockMenuClass.getMethod("consumeItem", int.class, int.class);

                int[] inputSlots = DynamicCrystalCatalog.inputSlots();
                List<?> recipes = (List<?>) getMachineRecipes.invoke(machine);

                for (Object recipe : recipes) {
                    ItemStack[] requiredInputs = (ItemStack[]) getInput.invoke(recipe);
                    if (requiredInputs.length != inputSlots.length) {
                        continue;
                    }

                    boolean matches = true;
                    for (int i = 0; i < inputSlots.length; i++) {
                        ItemStack actual = (ItemStack) getItemInSlot.invoke(menu, inputSlots[i]);
                        ItemStack expected = requiredInputs[i];
                        if (expected == null || expected.getType() == Material.AIR) {
                            if (actual != null && actual.getType() != Material.AIR) {
                                matches = false;
                                break;
                            }
                            continue;
                        }
                        if (actual == null || actual.getType() == Material.AIR
                            || actual.getAmount() < expected.getAmount()
                            || !sameSlimefunItem(actual, expected, getByItem, getId)) {
                            matches = false;
                            break;
                        }
                    }
                    if (!matches) {
                        continue;
                    }

                    ItemStack[] outputs = (ItemStack[]) getOutput.invoke(recipe);
                    if (outputs.length != 1) {
                        continue;
                    }

                    ItemStack result = outputs[0];
                    int[] outputSlots = {24, 25};
                    int availableOutputCapacity = 0;
                    Class<?> chestMenuUtils = Class.forName(
                        "io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils", true, loader
                    );
                    ItemStack outputTexture = (ItemStack) chestMenuUtils
                        .getMethod("getOutputSlotTexture").invoke(null);
                    boolean outputCanFit = true;

                    for (int outputSlot : outputSlots) {
                        ItemStack currentOutput = (ItemStack) getItemInSlot.invoke(menu, outputSlot);
                        if (currentOutput == null || currentOutput.getType() == org.bukkit.Material.AIR
                            || currentOutput.isSimilar(outputTexture)) {
                            availableOutputCapacity += result.getMaxStackSize();
                        } else if (sameSlimefunItem(currentOutput, result, getByItem, getId)) {
                            availableOutputCapacity += result.getMaxStackSize() - currentOutput.getAmount();
                        } else {
                            outputCanFit = false;
                            break;
                        }
                    }

                    if (!outputCanFit || availableOutputCapacity < result.getAmount()) {
                        continue;
                    }

                    for (int i = 0; i < inputSlots.length; i++) {
                        ItemStack requiredInput = requiredInputs[i];
                        if (requiredInput != null && requiredInput.getType() != Material.AIR) {
                            consumeItem.invoke(menu, inputSlots[i], requiredInput.getAmount());
                        }
                    }
                    return recipe;
                }
                return null;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not match a Dynamic Crystal Former recipe.", exception);
            }
        }

        private static boolean sameSlimefunItem(
            ItemStack first, ItemStack second, Method getByItem, Method getId
        ) throws ReflectiveOperationException {
            Object firstItem = getByItem.invoke(null, first);
            Object secondItem = getByItem.invoke(null, second);
            if (firstItem == null || secondItem == null) {
                return false;
            }
            return getId.invoke(firstItem).equals(getId.invoke(secondItem));
        }
    }

    public static final class MachineMenuConfigurator {

        private static boolean contains(int[] slots, int target) {
            for (int slot : slots) {
                if (slot == target) {
                    return true;
                }
            }
            return false;
        }

        public void configure(@This Object machine, @Argument(0) Object preset) {
            try {
                ClassLoader loader = machine.getClass().getClassLoader();
                Class<?> chestMenuUtils = Class.forName(CHEST_MENU_UTILS_CLASS, true, loader);
                Class<?> clickHandlerClass = Class.forName(MENU_CLICK_HANDLER_CLASS, true, loader);
                Class<?> advancedHandlerClass = Class.forName(ADVANCED_MENU_CLICK_HANDLER_CLASS, true, loader);

                ItemStack background = (ItemStack) chestMenuUtils.getMethod("getBackground").invoke(null);
                ItemStack inputTexture = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
                ItemMeta inputMeta = inputTexture.getItemMeta();
                inputMeta.setDisplayName(ChatColor.DARK_BLUE + "Crystal Inputs");
                inputTexture.setItemMeta(inputMeta);

                ItemStack outputTexture = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
                ItemMeta outputMeta = outputTexture.getItemMeta();
                outputMeta.setDisplayName(ChatColor.GOLD + "Crystal Output");
                outputTexture.setItemMeta(outputMeta);

                Object emptyClickHandler = chestMenuUtils.getMethod("getEmptyClickHandler").invoke(null);

                Object outputHandler = Proxy.newProxyInstance(
                    advancedHandlerClass.getClassLoader(),
                    new Class<?>[] {advancedHandlerClass},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("toString")) return "DynamicCrystalFormerOutputHandler";
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        if (method.getName().equals("equals")) return proxy == arguments[0];
                        if (!method.getName().equals("onClick")) return null;

                        // Match AContainer's normal output-slot behavior:
                        // prevent placing items into the output, but allow taking completed products.
                        if (arguments.length == 4) return false;
                        if (arguments.length == 5) {
                            org.bukkit.event.inventory.InventoryClickEvent event =
                                (org.bukkit.event.inventory.InventoryClickEvent) arguments[0];
                            ItemStack clickedItem = event.getCurrentItem();

                            ItemStack cursor = (ItemStack) arguments[3];
                            return cursor == null || cursor.getType() == null || cursor.getType() == Material.AIR;
                        }
                        return false;
                    }
                );

                Method addItem = preset.getClass().getMethod(
                    "addItem", int.class, ItemStack.class, clickHandlerClass
                );

                int[] inputSlots = DynamicCrystalCatalog.inputSlots();
                int[] inputMarkers = {0, 1, 2, 3, 4, 9, 13, 18, 27, 31, 36, 37, 38, 39, 40};
                int[] outputMarkers = {14, 15, 16, 17, 23, 26, 32, 33, 34, 35};

                for (int slot = 0; slot < 54; slot++) {
                    boolean isInputSlot = contains(inputSlots, slot);
                    boolean isInputMarker = contains(inputMarkers, slot);
                    boolean isOutputMarker = contains(outputMarkers, slot);
                    boolean isOpenOutput = slot == 24 || slot == 25;

                    // Keep every ingredient and product slot genuinely empty and usable.
                    if (!isInputSlot && !isInputMarker && !isOutputMarker && !isOpenOutput
                        && slot != DynamicCrystalCatalog.PROGRESS_SLOT && slot != 5) {
                        addItem.invoke(preset, slot, background.clone(), emptyClickHandler);
                    }
                }

                // Colored panes identify the input/output areas, but never occupy usable slots.
                for (int slot : inputMarkers) {
                    addItem.invoke(preset, slot, inputTexture.clone(), emptyClickHandler);
                }
                for (int slot : outputMarkers) {
                    addItem.invoke(preset, slot, outputTexture.clone(), emptyClickHandler);
                }

                // Match Slimefun AContainer: block inserting items, allow collecting completed output.
                Method addMenuClickHandler = preset.getClass().getMethod(
                    "addMenuClickHandler", int.class, clickHandlerClass
                );
                addMenuClickHandler.invoke(preset, 24, outputHandler);
                addMenuClickHandler.invoke(preset, 25, outputHandler);

                ItemStack progress = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta progressMeta = progress.getItemMeta();
                progressMeta.setDisplayName(" ");
                progress.setItemMeta(progressMeta);
                addItem.invoke(preset, DynamicCrystalCatalog.PROGRESS_SLOT, progress, emptyClickHandler);

                ItemStack arrow = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta = arrow.getItemMeta();
                arrowMeta.setDisplayName(ChatColor.GOLD + "Output");
                arrow.setItemMeta(arrowMeta);
                addItem.invoke(preset, 5, arrow, emptyClickHandler);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not create the Dynamic Crystal Former menu.", exception);
            }
        }
    }}
