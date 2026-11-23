package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class NuclearCompressor {

    public static final String MACHINE_ID = "GOAT_NUCLEAR_COMPRESSOR";
    public static final String MACHINE_NAME = "Nuclear Compressor";
    public static final String OUTPUT_ID = "GOAT_NUCLEAR_UU_MATTER";
    public static final String OUTPUT_NAME = "Nuclear UU Matter";

    private static final String ITEM_GROUP_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.ItemGroup";
    private static final String ITEM_STACK_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack";
    private static final String SLIMEFUN_ITEM_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem";
    private static final String RECIPE_TYPE_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType";
    private static final String A_CONTAINER_CLASS =
        "me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer";
    private static final String RADIOACTIVE_ITEM_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.items.RadioactiveItem";
    private static final String RADIOACTIVITY_CLASS =
        "io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity";

    private static final String UU_MATTER_ID = "UU_MATTER";
    private static final int ENERGY_CAPACITY = 16_000_000;
    private static final int ENERGY_PER_SLIMEFUN_TICK = 500_000;
    private static final int UU_MATTER_PER_TICK = 64;
    private static final int UU_MATTER_REQUIRED = 1_000_000;
    private static final int[] INPUT_SLOTS = {19, 20};
    private static final int[] OUTPUT_SLOTS = {24, 25};

    private NuclearCompressor() {
    }

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object itemGroup, Object addon)
        throws ReflectiveOperationException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> addonClass = Class.forName(
            "io.github.thebusybiscuit.slimefun4.api.SlimefunAddon", true, slimefunLoader
        );
        Method registerItem = slimefunItemClass.getMethod("register", addonClass);
        Object nullRecipe = recipeTypeClass.getField("NULL").get(null);

        ItemStack nuclearUuMatterStack = createSlimefunStack(
            slimefunLoader,
            OUTPUT_ID,
            Material.HEART_OF_THE_SEA,
            ChatColor.GOLD + OUTPUT_NAME,
            new String[] {
                ChatColor.GRAY + "Extremely deadly. Wear protection",
                ChatColor.RED + "☢ Radioactive"
            }
        );

        Class<?> radioactivityClass = Class.forName(RADIOACTIVITY_CLASS, true, slimefunLoader);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object veryDeadly = Enum.valueOf((Class<? extends Enum>) radioactivityClass.asSubclass(Enum.class), "VERY_DEADLY");
        Class<?> radioactiveItemClass = Class.forName(RADIOACTIVE_ITEM_CLASS, true, slimefunLoader);
        Constructor<?> radioactiveConstructor = radioactiveItemClass.getConstructor(
            itemGroupClass, radioactivityClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object nuclearUuMatter = radioactiveConstructor.newInstance(
            itemGroup, veryDeadly, nuclearUuMatterStack, nullRecipe, new ItemStack[0]
        );
        registerItem.invoke(nuclearUuMatter, addon);

        ItemStack machineStack = createSlimefunStack(
            slimefunLoader,
            MACHINE_ID,
            Material.BLAST_FURNACE,
            ChatColor.GOLD + MACHINE_NAME,
            new String[] {
                ChatColor.GRAY + "Lite Expansion has nothing on this.",
                ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
                ChatColor.GRAY + "Energy Buffer: 16,000,000 J",
                ChatColor.DARK_RED + "Breaking this machine will cause all stored UU matter to be lost"
            }
        );

        ItemStack[] craftingRecipe = resolveRecipe(slimefunLoader);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);
        ItemStack progressBar = createProgressItem();

        Class<?> machineClass = createMachineClass(
            plugin.getClass().getClassLoader(), slimefunLoader, progressBar
        );
        Constructor<?> machineConstructor = machineClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object machine = machineConstructor.newInstance(
            itemGroup, machineStack, enhancedCraftingTable, craftingRecipe
        );

        machineClass.getMethod("setCapacity", int.class).invoke(machine, ENERGY_CAPACITY);
        machineClass.getMethod("setEnergyConsumption", int.class)
            .invoke(machine, ENERGY_PER_SLIMEFUN_TICK);
        machineClass.getMethod("setProcessingSpeed", int.class).invoke(machine, 1);
        registerItem.invoke(machine, addon);

        plugin.getLogger().info(
            "[G.O.A.T. Industries] Nuclear Compressor and VERY_DEADLY Nuclear UU Matter registered."
        );
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
            .name("com.boomer1.goatindustries.generated.NuclearCompressorMachine")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .method(ElementMatchers.named("getProgressBar").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(progressBar))
            .method(ElementMatchers.named("getMachineIdentifier").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(MACHINE_ID))
            // AContainer's normal break handler drops getInputSlots(). Returning no input slots
            // intentionally prevents stored UU Matter from being refunded when the machine breaks.
            .method(ElementMatchers.named("getInputSlots").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(new int[0]))
            .method(ElementMatchers.named("getOutputSlots").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(OUTPUT_SLOTS))
            .method(ElementMatchers.named("tick").and(ElementMatchers.takesArguments(Block.class)))
            .intercept(MethodDelegation.to(new NuclearCompressorTicker()))
            .make()
            .load(pluginLoader, ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }

    private static ItemStack createSlimefunStack(
        ClassLoader loader, String id, Material material, String displayName, String[] lore
    ) throws ReflectiveOperationException {
        Class<?> stackClass = Class.forName(ITEM_STACK_CLASS, true, loader);
        Constructor<?> constructor = stackClass.getConstructor(
            String.class, Material.class, String.class, String[].class
        );
        return (ItemStack) constructor.newInstance(id, material, displayName, lore);
    }

    private static ItemStack[] resolveRecipe(ClassLoader loader) throws ReflectiveOperationException {
        return new ItemStack[] {
            resolveSlimefunItem(loader, "NUCLEAR_REACTOR"),
            resolveSlimefunItem(loader, "PLUTONIUM"),
            resolveSlimefunItem(loader, "NUCLEAR_REACTOR"),
            resolveSlimefunItem(loader, "PLUTONIUM"),
            resolveSlimefunItem(loader, "IE_INFINITY_CAPACITOR"),
            resolveSlimefunItem(loader, "PLUTONIUM"),
            resolveSlimefunItem(loader, "NUCLEAR_REACTOR"),
            resolveSlimefunItem(loader, "PLUTONIUM"),
            resolveSlimefunItem(loader, "NUCLEAR_REACTOR")
        };
    }

    private static ItemStack resolveSlimefunItem(ClassLoader loader, String id)
        throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
        Object item = slimefunItemClass.getMethod("getById", String.class).invoke(null, id);
        if (item == null) {
            throw new IllegalStateException("Required Nuclear Compressor recipe ingredient is not registered: " + id);
        }
        return ((ItemStack) slimefunItemClass.getMethod("getItem").invoke(item)).clone();
    }

    private static ItemStack createProgressItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "UU Matter Processing");
        item.setItemMeta(meta);
        return item;
    }

    public static final class NuclearCompressorTicker {

        @RuntimeType
        public void tick(@This Object machine, @Argument(0) Block block) {
            try {
                ClassLoader loader = machine.getClass().getClassLoader();
                Class<?> blockStorageClass = Class.forName(
                    "me.mrCookieSlime.Slimefun.api.BlockStorage", true, loader
                );
                Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
                Class<?> blockMenuClass = Class.forName(
                    "me.mrCookieSlime.Slimefun.api.inventory.BlockMenu", true, loader
                );
                Object menu = blockStorageClass.getMethod("getInventory", Block.class).invoke(null, block);
                if (menu == null) {
                    return;
                }

                Location location = block.getLocation();
                Method getCharge = machine.getClass().getMethod("getCharge", Location.class);
                Method setCharge = machine.getClass().getMethod("setCharge", Location.class, int.class);
                Method getItemInSlot = blockMenuClass.getMethod("getItemInSlot", int.class);
                Method consumeItem = blockMenuClass.getMethod("consumeItem", int.class, int.class);
                Method pushItem = blockMenuClass.getMethod("pushItem", ItemStack.class, int[].class);
                Method getLocationInfo = blockStorageClass.getMethod("getLocationInfo", Location.class);
                Method addBlockInfo = blockStorageClass.getMethod(
                    "addBlockInfo", Location.class, String.class, String.class
                );

                Object config = getLocationInfo.invoke(null, location);
                String storedText = (String) config.getClass().getMethod("getString", String.class)
                    .invoke(config, "nuclear_uu_matter_progress");
                int stored = storedText == null ? 0 : Integer.parseInt(storedText);

                ItemStack output = resolveRegisteredItem(slimefunItemClass, OUTPUT_ID);
                ItemStack outputProbe = output.clone();
                outputProbe.setAmount(1);

                if (stored >= UU_MATTER_REQUIRED) {
                    if (canFitOutput(loader, menu, outputProbe)) {
                        pushItem.invoke(menu, outputProbe, OUTPUT_SLOTS);
                        addBlockInfo.invoke(null, location, "nuclear_uu_matter_progress", "0");
                        updateProgress(menu, blockMenuClass, 0);
                    } else {
                        updateProgress(menu, blockMenuClass, stored);
                    }
                    return;
                }

                int available = 0;
                ItemStack[] inputStacks = new ItemStack[INPUT_SLOTS.length];
                for (int i = 0; i < INPUT_SLOTS.length; i++) {
                    ItemStack stack = (ItemStack) getItemInSlot.invoke(menu, INPUT_SLOTS[i]);
                    inputStacks[i] = stack;
                    if (isUuMatter(slimefunItemClass, stack)) {
                        available += stack.getAmount();
                    }
                }

                if (available <= 0) {
                    updateProgress(menu, blockMenuClass, stored);
                    return;
                }

                int energyCost = (int) machine.getClass().getMethod("getEnergyConsumption").invoke(machine);
                int charge = (int) getCharge.invoke(machine, location);
                if (charge < energyCost) {
                    updateProgress(menu, blockMenuClass, stored);
                    return;
                }

                int amountToProcess = Math.min(UU_MATTER_PER_TICK,
                    Math.min(available, UU_MATTER_REQUIRED - stored));
                int remaining = amountToProcess;
                for (int i = 0; i < INPUT_SLOTS.length && remaining > 0; i++) {
                    ItemStack stack = inputStacks[i];
                    if (!isUuMatter(slimefunItemClass, stack)) {
                        continue;
                    }
                    int consumed = Math.min(remaining, stack.getAmount());
                    consumeItem.invoke(menu, INPUT_SLOTS[i], consumed);
                    remaining -= consumed;
                }

                setCharge.invoke(machine, location, charge - energyCost);
                stored += amountToProcess;
                addBlockInfo.invoke(null, location, "nuclear_uu_matter_progress", Integer.toString(stored));

                if (stored >= UU_MATTER_REQUIRED && canFitOutput(loader, menu, outputProbe)) {
                    pushItem.invoke(menu, outputProbe, OUTPUT_SLOTS);
                    stored = 0;
                    addBlockInfo.invoke(null, location, "nuclear_uu_matter_progress", "0");
                }
                updateProgress(menu, blockMenuClass, stored);
            } catch (ReflectiveOperationException | NumberFormatException exception) {
                throw new IllegalStateException("Nuclear Compressor processing failed at " + block.getLocation(), exception);
            }
        }

        private static boolean isUuMatter(Class<?> slimefunItemClass, ItemStack stack)
            throws ReflectiveOperationException {
            if (stack == null || stack.getType() == Material.AIR) {
                return false;
            }
            Object item = slimefunItemClass.getMethod("getByItem", ItemStack.class).invoke(null, stack);
            return item != null && "UU_MATTER".equals(
                slimefunItemClass.getMethod("getId").invoke(item)
            );
        }

        private static ItemStack resolveRegisteredItem(Class<?> slimefunItemClass, String id)
            throws ReflectiveOperationException {
            Object item = slimefunItemClass.getMethod("getById", String.class).invoke(null, id);
            if (item == null) {
                throw new IllegalStateException("Registered Nuclear UU Matter output could not be found: " + id);
            }
            return ((ItemStack) slimefunItemClass.getMethod("getItem").invoke(item)).clone();
        }

        private static boolean canFitOutput(ClassLoader loader, Object menu, ItemStack output)
            throws ReflectiveOperationException {
            Class<?> blockMenuClass = menu.getClass();
            Object inventory = blockMenuClass.getMethod("toInventory").invoke(menu);
            Class<?> invUtilsClass = Class.forName(
                "io.github.bakedlibs.dough.inventory.InvUtils", true, loader
            );
            Method fitAll = invUtilsClass.getMethod(
                "fitAll", org.bukkit.inventory.Inventory.class, ItemStack[].class, int[].class
            );
            return (boolean) fitAll.invoke(null, inventory, new ItemStack[] {output}, OUTPUT_SLOTS);
        }

        private static void updateProgress(Object menu, Class<?> blockMenuClass, int stored)
            throws ReflectiveOperationException {
            ItemStack status = new ItemStack(Material.CLOCK);
            ItemMeta meta = status.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "UU Matter Processing");
            meta.setLore(java.util.List.of(
                ChatColor.GRAY + String.format("%,d / %,d UU Matter", stored, UU_MATTER_REQUIRED),
                stored >= UU_MATTER_REQUIRED
                    ? ChatColor.GREEN + "Ready - waiting for output space"
                    : ChatColor.GRAY + "Stored UU Matter is lost if this machine is broken"
            ));
            status.setItemMeta(meta);
            blockMenuClass.getMethod("replaceExistingItem", int.class, ItemStack.class)
                .invoke(menu, 22, status);
        }
    }
}
