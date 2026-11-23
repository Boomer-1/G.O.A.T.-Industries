package com.boomer1.goatindustries;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlimefunContentRegistrar {

    private static final String SLIMEFUN_ADDON = "io.github.thebusybiscuit.slimefun4.api.SlimefunAddon";
    private static final String ITEM_GROUP = "io.github.thebusybiscuit.slimefun4.api.items.ItemGroup";
    private static final String NESTED_ITEM_GROUP = "io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup";
    private static final String SUB_ITEM_GROUP = "io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup";
    private static final String SLIMEFUN_ITEM_STACK =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack";
    private static final String SLIMEFUN_ITEM = "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem";
    private static final String RECIPE_TYPE = "io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType";

    private SlimefunContentRegistrar() {
    }

    public static void register(JavaPlugin plugin, Plugin slimefun, Plugin emcTech)
        throws ReflectiveOperationException, IOException {
        ClassLoader loader = slimefun.getClass().getClassLoader();

        Class<?> addonClass = Class.forName(SLIMEFUN_ADDON, true, loader);
        Object addon = createAddonProxy(addonClass, plugin);

        ItemStack groupIcon = new ItemStack(Material.REINFORCED_DEEPSLATE);
        ItemMeta groupMeta = groupIcon.getItemMeta();
        groupMeta.setDisplayName(ChatColor.GOLD + "G.O.A.T. Industries");
        groupIcon.setItemMeta(groupMeta);

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP, true, loader);
        Class<?> nestedItemGroupClass = Class.forName(NESTED_ITEM_GROUP, true, loader);
        Constructor<?> groupConstructor = nestedItemGroupClass.getConstructor(NamespacedKey.class, ItemStack.class);
        Object itemGroup = groupConstructor.newInstance(new NamespacedKey(plugin, "goat_industries"), groupIcon);

        Class<?> subItemGroupClass = Class.forName(SUB_ITEM_GROUP, true, loader);
        Constructor<?> subGroupConstructor = subItemGroupClass.getConstructor(
            NamespacedKey.class, nestedItemGroupClass, ItemStack.class
        );

        Object trophyGroup = createSubGroup(plugin, subGroupConstructor, itemGroup, "goat_trophy",
            Material.GOAT_SPAWN_EGG, ChatColor.GOLD + "The Goat Trophy");
        Object machinesGroup = createSubGroup(plugin, subGroupConstructor, itemGroup, "machines",
            Material.BLAST_FURNACE, ChatColor.GOLD + "Machines");
        Object resourcesGroup = createSubGroup(plugin, subGroupConstructor, itemGroup, "resources",
            Material.DIAMOND, ChatColor.GOLD + "Resources");

        NamespacedKey trophyMarker = new NamespacedKey(plugin, "goat_trophy");
        ItemStack trophyStack = createSlimefunStack(loader, "GOAT_TROPHY", Material.GOAT_SPAWN_EGG,
            ChatColor.GOLD + "G.O.A.T. Trophy",
            new String[] {
                ChatColor.GRAY + "The ultimate trophy.",
                ChatColor.GRAY + "Requires a five-tier Netherite pyramid.",
                ChatColor.YELLOW + "Right-click the top Netherite Block to place."
            });
        ItemMeta trophyMeta = trophyStack.getItemMeta();
        trophyMeta.getPersistentDataContainer().set(trophyMarker, PersistentDataType.BYTE, (byte) 1);
        trophyStack.setItemMeta(trophyMeta);

        ItemStack guideStack = createSlimefunStack(loader, "GOAT_PYRAMID_INSTRUCTIONS", Material.BOOK,
            ChatColor.AQUA + "G.O.A.T. Pyramid Instructions",
            new String[] {
                ChatColor.GRAY + "Build five centered square layers",
                ChatColor.GRAY + "using Netherite Blocks.",
                ChatColor.WHITE + "Top to bottom:",
                ChatColor.YELLOW + "1 x 1  -   1 block",
                ChatColor.YELLOW + "3 x 3  -   9 blocks",
                ChatColor.YELLOW + "5 x 5  -  25 blocks",
                ChatColor.YELLOW + "7 x 7  -  49 blocks",
                ChatColor.YELLOW + "9 x 9  -  81 blocks",
                ChatColor.GOLD + "Total: 165 Netherite Blocks",
                ChatColor.GRAY + "Center every layer above the previous layer.",
                ChatColor.GREEN + "Hold the G.O.A.T. Trophy and",
                ChatColor.GREEN + "right-click the single top block.",
                ChatColor.DARK_RED + "Guide item only - not a crafting ingredient."
            });

        ItemStack emcDiamondStack = createSlimefunStack(loader, "EMC_DIAMOND", Material.DIAMOND_BLOCK,
            ChatColor.AQUA + "EMC Diamond",
            new String[] {
                ChatColor.GRAY + "A reward for reaching",
                ChatColor.GOLD + "1,000,000,000 EMC.",
                ChatColor.GRAY + "Awarded once per player."
            });

        Class<?> slimefunItemStackClass = Class.forName(SLIMEFUN_ITEM_STACK, true, loader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE, true, loader);
        Object nullRecipe = recipeTypeClass.getField("NULL").get(null);

        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM, true, loader);
        Constructor<?> itemConstructor = slimefunItemClass.getConstructor(itemGroupClass,
            slimefunItemStackClass, recipeTypeClass, ItemStack[].class);

        Object guideItem = itemConstructor.newInstance(trophyGroup, guideStack, nullRecipe, new ItemStack[0]);
        slimefunItemClass.getMethod("register", addonClass).invoke(guideItem, addon);

        Object emcDiamondItem = itemConstructor.newInstance(resourcesGroup, emcDiamondStack, nullRecipe,
            new ItemStack[0]);
        slimefunItemClass.getMethod("register", addonClass).invoke(emcDiamondItem, addon);

        UniversalPowerCore.register(plugin, loader, resourcesGroup, addon);
        GalacticSpaceball.register(plugin, loader, resourcesGroup, addon);
        DynamicCrystalFormer.register(plugin, loader, machinesGroup, resourcesGroup, addon);
        NuclearCompressor.register(plugin, loader, machinesGroup, resourcesGroup, addon);
        ImplosivePress.register(plugin, loader, machinesGroup, resourcesGroup, addon);
        SuperSupremeCore.register(plugin, loader, resourcesGroup, addon);
        SuperDaxi.register(plugin, loader, resourcesGroup, addon);

        ItemStack[] trophyRecipe = resolveRecipe(loader, new String[] {
            "GOAT_SUPER_DAXI",
            "IE_INFINITY_SINGULARITY_REACTOR",
            "GOAT_SUPER_SUPREME_CORE",
            "GOAT_IMPLOSIVE_COBBLESTONE",
            "GOAT_NUCLEAR_UU_MATTER",
            "GOAT_GALACTIC_SPACEBALL",
            "GOAT_UNIVERSAL_POWER_CORE",
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID,
            "EMC_DIAMOND"
        });
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);
        Object trophyItem = itemConstructor.newInstance(
            trophyGroup, trophyStack, enhancedCraftingTable, trophyRecipe
        );
        slimefunItemClass.getMethod("register", addonClass).invoke(trophyItem, addon);

        plugin.getServer().getPluginManager().registerEvents(new DynamicCrystalPlacementListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GoatTrophyListener(plugin), plugin);
        new EmcDiamondRewardManager(plugin, emcTech, emcDiamondStack).start();

    }

    private static Object createSubGroup(JavaPlugin plugin, Constructor<?> constructor, Object parent,
                                         String key, Material icon, String displayName)
        throws ReflectiveOperationException {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return constructor.newInstance(new NamespacedKey(plugin, key), parent, item);
    }

    private static ItemStack[] resolveRecipe(ClassLoader loader, String[] itemIds)
        throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM, true, loader);
        java.lang.reflect.Method getById = slimefunItemClass.getMethod("getById", String.class);
        java.lang.reflect.Method getItem = slimefunItemClass.getMethod("getItem");

        ItemStack[] recipe = new ItemStack[itemIds.length];
        for (int slot = 0; slot < itemIds.length; slot++) {
            Object slimefunItem = getById.invoke(null, itemIds[slot]);
            if (slimefunItem == null) {
                throw new IllegalStateException(
                    "Required G.O.A.T. Trophy ingredient is not registered: " + itemIds[slot]
                );
            }

            recipe[slot] = ((ItemStack) getItem.invoke(slimefunItem)).clone();
        }
        return recipe;
    }

    private static ItemStack createSlimefunStack(ClassLoader loader, String id, Material material,
                                                  String displayName, String[] lore)
        throws ReflectiveOperationException {
        Class<?> stackClass = Class.forName(SLIMEFUN_ITEM_STACK, true, loader);
        Constructor<?> constructor = stackClass.getConstructor(String.class, Material.class,
            String.class, String[].class);
        return (ItemStack) constructor.newInstance(id, material, displayName, lore);
    }

    private static Object createAddonProxy(Class<?> addonClass, JavaPlugin plugin) {
        InvocationHandler handler = (proxy, method, arguments) -> {
            return switch (method.getName()) {
                case "getJavaPlugin" -> plugin;
                case "getBugTrackerURL" -> null;
                case "getName" -> plugin.getName();
                case "getPluginVersion" -> plugin.getDescription().getVersion();
                case "getLogger" -> plugin.getLogger();
                case "hasDependency" -> hasDependency(plugin, (String) arguments[0]);
                case "toString" -> plugin.getName();
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> throw new UnsupportedOperationException(
                    "Unsupported SlimefunAddon method: " + method.getName());
            };
        };

        return Proxy.newProxyInstance(addonClass.getClassLoader(), new Class<?>[] {addonClass}, handler);
    }

    private static boolean hasDependency(JavaPlugin plugin, String dependency) {
        if (plugin.getName().equalsIgnoreCase(dependency)) {
            return true;
        }

        return plugin.getDescription().getDepend().contains(dependency)
            || plugin.getDescription().getSoftDepend().contains(dependency);
    }
}
