package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImplosivePress {

    public static final String MACHINE_ID = "GOAT_IMPLOSIVE_PRESS";
    public static final String MACHINE_NAME = "Implosive Press";
    public static final String OUTPUT_ID = "GOAT_IMPLOSIVE_COBBLESTONE";
    public static final String OUTPUT_NAME = "Implosive Cobblestone";

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
    private static final String UNPLACEABLE_BLOCK_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock";

    private static final String[] CRAFTING_INGREDIENT_IDS = {
        "IE_VOID_CAPACITOR",
        "MACERATOR",
        "IE_VOID_CAPACITOR",
        "SUPREME_VENTUS_GENERATOR",
        "CARBONADO_EDGED_FURNACE",
        "SUPREME_VENTUS_GENERATOR",
        "IE_VOID_CAPACITOR",
        "MACERATOR",
        "IE_VOID_CAPACITOR"
    };

    private static final String COMPRESSED_COBBLESTONE_ID = "MOMOTECH_COBBLESTONE49";
    private static final int ENERGY_CAPACITY = 16_000_000;
    private static final int ENERGY_PER_SLIMEFUN_TICK = 500_000;
    private static final int PROCESSING_SECONDS = 30;

    private ImplosivePress() {
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

        ItemStack machineStack = createSlimefunStack(
            slimefunLoader,
            MACHINE_ID,
            Material.WAXED_OXIDIZED_COPPER_BULB,
            ChatColor.GOLD + MACHINE_NAME,
            new String[] {
                ChatColor.GRAY + "There is absolutely no way to make anything more compact"
            }
        );

        ItemStack outputStack = createSlimefunStack(
            slimefunLoader,
            OUTPUT_ID,
            Material.COBBLESTONE,
            ChatColor.GOLD + OUTPUT_NAME,
            new String[] {
                ChatColor.GRAY + "Ready to implode at any given moment"
            }
        );
        ItemMeta outputMeta = outputStack.getItemMeta();
        outputMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        outputMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        outputStack.setItemMeta(outputMeta);

        Class<?> unplaceableBlockClass = Class.forName(UNPLACEABLE_BLOCK_CLASS, true, slimefunLoader);
        Constructor<?> outputConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object nullRecipe = recipeTypeClass.getField("NULL").get(null);
        Object outputItem = outputConstructor.newInstance(
            itemGroup, outputStack, nullRecipe, new ItemStack[0]
        );
        Method registerItem = slimefunItemClass.getMethod("register", addonClass);
        registerItem.invoke(outputItem, addon);

        ItemStack[] craftingRecipe = resolveItems(slimefunLoader, CRAFTING_INGREDIENT_IDS);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        ItemStack progressBar = new ItemStack(Material.CLOCK);
        ItemMeta progressMeta = progressBar.getItemMeta();
        progressMeta.setDisplayName(ChatColor.YELLOW + "Implosive Press");
        progressBar.setItemMeta(progressMeta);

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

        Method registerMachineRecipe = machineClass.getMethod(
            "registerRecipe", int.class, ItemStack[].class, ItemStack[].class
        );
        ItemStack compressedCobblestone = resolveItem(slimefunLoader, COMPRESSED_COBBLESTONE_ID);
        compressedCobblestone.setAmount(64);
        registerMachineRecipe.invoke(
            machine,
            PROCESSING_SECONDS,
            new ItemStack[] {compressedCobblestone},
            new ItemStack[] {outputStack.clone()}
        );

        registerItem.invoke(machine, addon);

        plugin.getServer().getPluginManager().registerEvents(
            new RedstoneProtectionListener(plugin, slimefunLoader), plugin
        );

        plugin.getLogger().info(
            "[G.O.A.T. Industries] Implosive Press registered with its 30-second compressed cobblestone recipe."
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
            .name("com.boomer1.goatindustries.generated.ImplosivePressMachine")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .method(ElementMatchers.named("getProgressBar").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(progressBar))
            .method(ElementMatchers.named("getMachineIdentifier").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(MACHINE_ID))
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

    private static ItemStack[] resolveItems(ClassLoader loader, String[] ids)
        throws ReflectiveOperationException {
        ItemStack[] items = new ItemStack[ids.length];
        for (int i = 0; i < ids.length; i++) {
            items[i] = resolveItem(loader, ids[i]);
        }
        return items;
    }

    private static ItemStack resolveItem(ClassLoader loader, String id)
        throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
        Object item = slimefunItemClass.getMethod("getById", String.class).invoke(null, id);
        if (item == null) {
            throw new IllegalStateException("Required Implosive Press ingredient is not registered: " + id);
        }
        return ((ItemStack) slimefunItemClass.getMethod("getItem").invoke(item)).clone();
    }

    public static final class RedstoneProtectionListener implements Listener {

        private final JavaPlugin plugin;
        private final ClassLoader slimefunLoader;
        private final java.util.Set<Location> presses = new java.util.HashSet<>();

        public RedstoneProtectionListener(JavaPlugin plugin, ClassLoader slimefunLoader) {
            this.plugin = plugin;
            this.slimefunLoader = slimefunLoader;
            loadExistingPresses();

            // Copper bulbs change their LIT block-state property inside Minecraft's
            // neighbor-update logic. Physics-event cancellation alone does not
            // reliably prevent that transition, so enforce the visual state directly.
            plugin.getServer().getScheduler().runTaskTimer(
                plugin, this::enforceUnlitState, 1L, 1L
            );
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
            if (event.getBlockPlaced().getType() != Material.WAXED_OXIDIZED_COPPER_BULB) {
                return;
            }

            Location location = event.getBlockPlaced().getLocation();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (isImplosivePress(location)) {
                    presses.add(location.clone());
                    enforceUnlit(location);
                }
            });
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
            presses.remove(event.getBlock().getLocation());
        }

        @EventHandler
        public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
            loadPressesInChunk(event.getChunk());
        }

        @EventHandler
        public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();
            presses.removeIf(location ->
                location.getWorld().equals(event.getWorld())
                    && (location.getBlockX() >> 4) == chunkX
                    && (location.getBlockZ() >> 4) == chunkZ
            );
        }

        private void loadExistingPresses() {
            for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                loadPressesFromWorld(world, -1, -1);
            }
        }

        private void loadPressesInChunk(org.bukkit.Chunk chunk) {
            loadPressesFromWorld(chunk.getWorld(), chunk.getX(), chunk.getZ());
        }

        private void loadPressesFromWorld(org.bukkit.World world, int chunkX, int chunkZ) {
            try {
                Class<?> blockStorageClass = Class.forName(
                    "me.mrCookieSlime.Slimefun.api.BlockStorage", true, slimefunLoader
                );
                java.util.Map<?, ?> storage = (java.util.Map<?, ?>) blockStorageClass
                    .getMethod("getRawStorage", org.bukkit.World.class)
                    .invoke(null, world);
                if (storage == null) {
                    return;
                }

                for (java.util.Map.Entry<?, ?> entry : storage.entrySet()) {
                    if (!(entry.getKey() instanceof Location location)) {
                        continue;
                    }
                    if (chunkX >= 0 && ((location.getBlockX() >> 4) != chunkX
                        || (location.getBlockZ() >> 4) != chunkZ)) {
                        continue;
                    }

                    Object config = entry.getValue();
                    String id = (String) config.getClass()
                        .getMethod("getString", String.class).invoke(config, "id");
                    if (MACHINE_ID.equals(id)) {
                        presses.add(location.clone());
                    }
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(
                    "Could not load registered Implosive Press locations for redstone protection.",
                    exception
                );
            }
        }

        private void enforceUnlitState() {
            java.util.Iterator<Location> iterator = presses.iterator();
            while (iterator.hasNext()) {
                Location location = iterator.next();
                if (location.getWorld() == null || !location.getWorld().isChunkLoaded(
                    location.getBlockX() >> 4, location.getBlockZ() >> 4
                )) {
                    iterator.remove();
                    continue;
                }

                Block block = location.getBlock();
                if (block.getType() != Material.WAXED_OXIDIZED_COPPER_BULB
                    || !isImplosivePress(location)) {
                    iterator.remove();
                    continue;
                }

                enforceUnlit(location);
            }
        }

        private void enforceUnlit(Location location) {
            Block block = location.getBlock();
            if (block.getType() != Material.WAXED_OXIDIZED_COPPER_BULB) {
                return;
            }

            BlockData data = block.getBlockData();
            if (data instanceof CopperBulb bulb && bulb.isLit()) {
                bulb.setLit(false);
                block.setBlockData(bulb, false);
            }
        }

        private boolean isImplosivePress(Location location) {
            try {
                Class<?> blockStorageClass = Class.forName(
                    "me.mrCookieSlime.Slimefun.api.BlockStorage", true, slimefunLoader
                );
                Object config = blockStorageClass.getMethod("getLocationInfo", Location.class)
                    .invoke(null, location);
                if (config == null) {
                    return false;
                }

                String id = (String) config.getClass().getMethod("getString", String.class)
                    .invoke(config, "id");
                return MACHINE_ID.equals(id);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(
                    "Could not identify the Implosive Press at " + location,
                    exception
                );
            }
        }
    }
}