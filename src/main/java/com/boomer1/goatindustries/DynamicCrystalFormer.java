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
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
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
            Material.FURNACE,
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
                    ChatColor.GRAY + "Created in the Dynamic Crystal Former",
                    ChatColor.GRAY + "Processing Time: 30 seconds"
                }
            );
            setHeadTexture(outputStack, output.textureUrl(), slimefunLoader);

            ItemStack[] inputs = resolveItems(slimefunLoader, DynamicCrystalCatalog.inputIds(output.rarity()));
            Object outputItem = outputItemConstructor.newInstance(itemGroup, outputStack, dynamicCrystalRecipeType, inputs);
            registerItem.invoke(outputItem, addon);

            registerMachineRecipe.invoke(
                machine,
                DynamicCrystalCatalog.PROCESSING_TICKS,
                inputs,
                new ItemStack[] {outputStack.clone()}
            );
        }

        plugin.getLogger().info("[G.O.A.T. Industries] Dynamic Crystal Former registered with six exact 3x3 crystal recipes.");
    }

    private static Class<?> createMachineClass(ClassLoader pluginLoader, ClassLoader slimefunLoader, ItemStack progressBar)
        throws ReflectiveOperationException {

        Class<?> aContainerClass = Class.forName(A_CONTAINER_CLASS, true, slimefunLoader);
        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Constructor<?> superConstructor = aContainerClass.getDeclaredConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );

        return new ByteBuddy()
            .subclass(aContainerClass)
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
            .intercept(FixedValue.value(new int[] {DynamicCrystalCatalog.OUTPUT_SLOT}))
            .method(ElementMatchers.named("findNextRecipe").and(ElementMatchers.takesArguments(1)))
            .intercept(MethodDelegation.to(new ExactRecipeMatcher()))
            .method(ElementMatchers.named("constructMenu").and(ElementMatchers.takesArguments(1)))
            .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.to(new MachineMenuConfigurator())))
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
            .subclass(slimefunItemClass)
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
                        if (actual == null || expected == null
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

                    ItemStack currentOutput = (ItemStack) getItemInSlot.invoke(menu, DynamicCrystalCatalog.OUTPUT_SLOT);
                    ItemStack result = outputs[0];
                    if (currentOutput != null
                        && (!sameSlimefunItem(currentOutput, result, getByItem, getId)
                            || currentOutput.getAmount() + result.getAmount() > result.getMaxStackSize())) {
                        continue;
                    }

                    for (int i = 0; i < inputSlots.length; i++) {
                        consumeItem.invoke(menu, inputSlots[i], requiredInputs[i].getAmount());
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

        public void configure(@This Object machine, @Argument(0) Object preset) {
            try {
                ClassLoader loader = machine.getClass().getClassLoader();
                Class<?> chestMenuUtils = Class.forName(CHEST_MENU_UTILS_CLASS, true, loader);
                Class<?> clickHandlerClass = Class.forName(MENU_CLICK_HANDLER_CLASS, true, loader);

                ItemStack background = (ItemStack) chestMenuUtils.getMethod("getBackground").invoke(null);
                ItemStack inputTexture = (ItemStack) chestMenuUtils.getMethod("getInputSlotTexture").invoke(null);
                ItemStack outputTexture = (ItemStack) chestMenuUtils.getMethod("getOutputSlotTexture").invoke(null);
                Object inputHandler = Proxy.newProxyInstance(
                    clickHandlerClass.getClassLoader(),
                    new Class<?>[] {clickHandlerClass},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("toString")) return "DynamicCrystalFormerInput";
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        if (method.getName().equals("equals")) return proxy == arguments[0];
                        return method.getName().equals("onClick");
                    }
                );
                Object outputHandler = preset.getClass()
                    .getMethod("getMenuClickHandler", int.class)
                    .invoke(preset, DynamicCrystalCatalog.OUTPUT_SLOT);

                Object denyHandler = Proxy.newProxyInstance(
                    clickHandlerClass.getClassLoader(),
                    new Class<?>[] {clickHandlerClass},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("toString")) return "DynamicCrystalFormerMenuDecoration";
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        if (method.getName().equals("equals")) return proxy == arguments[0];
                        return false;
                    }
                );

                Method addItem = preset.getClass().getMethod(
                    "addItem", int.class, ItemStack.class, clickHandlerClass
                );
                for (int slot = 0; slot < 54; slot++) {
                    addItem.invoke(preset, slot, background.clone(), denyHandler);
                }
                for (int slot : DynamicCrystalCatalog.inputSlots()) {
                    addItem.invoke(preset, slot, inputTexture.clone(), inputHandler);
                }
                addItem.invoke(preset, DynamicCrystalCatalog.OUTPUT_SLOT, outputTexture.clone(), outputHandler);

                ItemStack progress = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta progressMeta = progress.getItemMeta();
                progressMeta.setDisplayName(" ");
                progress.setItemMeta(progressMeta);
                addItem.invoke(preset, DynamicCrystalCatalog.PROGRESS_SLOT, progress, denyHandler);

                ItemStack arrow = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta = arrow.getItemMeta();
                arrowMeta.setDisplayName(ChatColor.GOLD + "Output");
                arrow.setItemMeta(arrowMeta);
                addItem.invoke(preset, 14, arrow, denyHandler);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not create the Dynamic Crystal Former menu.", exception);
            }
        }
    }
}
