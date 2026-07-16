package org.ltzin.utils;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;


public final class BukkitUtils {

    private static final Logger LOGGER = Logger.getLogger("VoidlessPlugin");

    private static final String NMS_VERSION;

    private static final boolean IS_1_9_PLUS;

    static {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = pkg.split("\\.");
        NMS_VERSION = parts.length >= 4 ? parts[3] : "unknown";

        boolean has19 = false;
        try {
            Class.forName("org.bukkit.Particle");
            has19 = true;
        } catch (ClassNotFoundException ignored) {}
        IS_1_9_PLUS = has19;
    }


    private static final Map<String, String> LEGACY_ALIAS = new LinkedHashMap<>();

    static {

        alias("WOOD_SWORD",       "WOODEN_SWORD");
        alias("WOOD_PICKAXE",     "WOODEN_PICKAXE");
        alias("WOOD_AXE",         "WOODEN_AXE");
        alias("WOOD_HOE",         "WOODEN_HOE");
        alias("WOOD_SPADE",       "WOODEN_SHOVEL");
        alias("GOLD_SWORD",       "GOLDEN_SWORD");
        alias("GOLD_PICKAXE",     "GOLDEN_PICKAXE");
        alias("GOLD_AXE",         "GOLDEN_AXE");
        alias("GOLD_HOE",         "GOLDEN_HOE");
        alias("GOLD_SPADE",       "GOLDEN_SHOVEL");
        alias("GOLD_HELMET",      "GOLDEN_HELMET");
        alias("GOLD_CHESTPLATE",  "GOLDEN_CHESTPLATE");
        alias("GOLD_LEGGINGS",    "GOLDEN_LEGGINGS");
        alias("GOLD_BOOTS",       "GOLDEN_BOOTS");
        alias("IRON_SPADE",       "IRON_SHOVEL");
        alias("DIAMOND_SPADE",    "DIAMOND_SHOVEL");
        alias("STONE_SPADE",      "STONE_SHOVEL");
        alias("SKULL_ITEM",       "PLAYER_HEAD");
        alias("INK_SACK",         "INK_SAC");
        alias("SULPHUR",          "GUNPOWDER");
        alias("SEEDS",            "WHEAT_SEEDS");
        alias("SUGAR_CANE",       "SUGAR_CANE");
        alias("NETHER_STALK",     "NETHER_WART");
        alias("BOOK_AND_QUILL",   "WRITABLE_BOOK");
        alias("FIREWORK",         "FIREWORK_ROCKET");
        alias("FIREWORK_CHARGE",  "FIREWORK_STAR");
        alias("DIODE",            "REPEATER");
        alias("WATCH",            "CLOCK");
        alias("EMPTY_MAP",        "MAP");
        alias("SNOW_BALL",        "SNOWBALL");
        alias("MONSTER_EGG",      "SPAWN_EGG");
        alias("BED",              "RED_BED");
        alias("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE");
        alias("HARD_CLAY",        "TERRACOTTA");
        alias("STAINED_CLAY",     "WHITE_TERRACOTTA");
        alias("WEB",              "COBWEB");
        alias("ENDER_STONE",      "END_STONE");
        alias("RED_ROSE",         "POPPY");
        alias("YELLOW_FLOWER",    "DANDELION");
        alias("DEAD_BUSH",        "DEAD_BUSH");
        alias("LONG_GRASS",       "GRASS");
        alias("WOOD",             "OAK_PLANKS");
        alias("LOG",              "OAK_LOG");
        alias("LEAVES",           "OAK_LEAVES");
        alias("WORKBENCH",        "CRAFTING_TABLE");
        alias("BURNING_FURNACE",  "FURNACE");
        alias("SIGN",             "OAK_SIGN");
        alias("SIGN_POST",        "OAK_SIGN");
        alias("WALL_SIGN",        "OAK_WALL_SIGN");
        alias("WOOD_DOOR",        "OAK_DOOR");
        alias("FENCE",            "OAK_FENCE");
        alias("FENCE_GATE",       "OAK_FENCE_GATE");
        alias("WOOD_STAIRS",      "OAK_STAIRS");
        alias("WOOD_PLATE",       "OAK_PRESSURE_PLATE");
        alias("WOOD_BUTTON",      "OAK_BUTTON");
        alias("WOOD_STEP",        "OAK_SLAB");
        alias("WOOD_DOUBLE_STEP", "OAK_SLAB");
        alias("PORK",             "PORKCHOP");
        alias("GRILLED_PORK",     "COOKED_PORKCHOP");
        alias("RAW_FISH",         "COD");
        alias("COOKED_FISH",      "COOKED_COD");
        alias("RAW_BEEF",         "BEEF");
        alias("COOKED_BEEF",      "COOKED_BEEF");
        alias("RAW_CHICKEN",      "CHICKEN");
        alias("STEP",             "STONE_SLAB");
        alias("DOUBLE_STEP",      "STONE_SLAB");
        alias("SMOOTH_BRICK",     "STONE_BRICKS");
        alias("THIN_GLASS",       "GLASS_PANE");
        alias("IRON_FENCE",       "IRON_BARS");
        alias("MYCEL",            "MYCELIUM");
        alias("WATER_LILY",       "LILY_PAD");
        alias("NETHER_BRICK",     "NETHER_BRICKS");
        alias("NETHER_FENCE",     "NETHER_BRICK_FENCE");
        alias("ENCHANTMENT_TABLE","ENCHANTING_TABLE");
        alias("ENDER_CHEST",      "ENDER_CHEST");
        alias("COBBLE_WALL",      "COBBLESTONE_WALL");
        alias("FLOWER_POT",       "FLOWER_POT");
        alias("QUARTZ_BLOCK",     "QUARTZ_BLOCK");
        alias("REDSTONE_COMPARATOR_OFF", "COMPARATOR");
        alias("REDSTONE_COMPARATOR_ON",  "COMPARATOR");
        alias("REDSTONE_COMPARATOR",     "COMPARATOR");
        alias("ACTIVATOR_RAIL",   "ACTIVATOR_RAIL");
        alias("DAYLIGHT_DETECTOR_INVERTED", "DAYLIGHT_DETECTOR");
        alias("PRISMARINE",       "PRISMARINE");
        alias("SEA_LANTERN",      "SEA_LANTERN");
        alias("CARPET",           "WHITE_CARPET");
        alias("PACKED_ICE",       "PACKED_ICE");
        alias("DOUBLE_PLANT",     "SUNFLOWER");
        alias("STANDING_BANNER",  "WHITE_BANNER");
        alias("WALL_BANNER",      "WHITE_WALL_BANNER");
        alias("RED_SANDSTONE",    "RED_SANDSTONE");
        alias("LEASH",            "LEAD");
        alias("ARMOR_STAND",      "ARMOR_STAND");
        alias("PRISMARINE_SHARD", "PRISMARINE_SHARD");
        alias("PRISMARINE_CRYSTALS", "PRISMARINE_CRYSTALS");
        alias("RABBIT_HIDE",      "RABBIT_HIDE");
        alias("IRON_BARDING",     "IRON_HORSE_ARMOR");
        alias("GOLD_BARDING",     "GOLDEN_HORSE_ARMOR");
        alias("DIAMOND_BARDING",  "DIAMOND_HORSE_ARMOR");
        alias("NETHER_BRICK_ITEM","NETHER_BRICK");
        alias("EXP_BOTTLE",       "EXPERIENCE_BOTTLE");
        alias("FIREBALL",         "FIRE_CHARGE");
        alias("CARROT_STICK",     "CARROT_ON_A_STICK");
        alias("GOLD_RECORD",      "MUSIC_DISC_13");
        alias("GREEN_RECORD",     "MUSIC_DISC_CAT");
        alias("COAL_BLOCK",       "COAL_BLOCK");
        alias("HAY_BLOCK",        "HAY_BLOCK");
    }

    private static void alias(String from, String to) {
        LEGACY_ALIAS.put(from.toUpperCase(), to.toUpperCase());
    }


    /**
     * Configs antigas (de antes do 1.13) as vezes guardam o item como o ID
     * NUMERICO puro do Minecraft (ex: "384", "351:8") em vez do nome do
     * Material. A partir do 1.13 o Bukkit removeu de vez o suporte a IDs
     * numericos da API publica, entao nao tem "Material.getMaterial(int)"
     * pra chamar mais — essa tabela é a nossa reconstrução manual da lista
     * clássica de IDs (blocos 1-255, itens 256-452) pra conseguir traduzir
     * esses configs antigos pro Material atual.
     */
    private static final Map<Integer, String> NUMERIC_LEGACY_ID = new HashMap<>();

    /** Onde o "data value" muda o item (cor da lã, tipo de madeira, etc). */
    private static final Map<Integer, Map<Short, String>> NUMERIC_LEGACY_VARIANTS = new HashMap<>();

    private static void id(int id, String legacyName) {
        NUMERIC_LEGACY_ID.put(id, legacyName.toUpperCase());
    }

    private static void variant(int id, int data, String legacyName) {
        NUMERIC_LEGACY_VARIANTS
                .computeIfAbsent(id, k -> new HashMap<>())
                .put((short) data, legacyName.toUpperCase());
    }

    static {
        // ---- Blocos (1-255) ----
        id(1, "STONE");           variant(1, 1, "GRANITE");        variant(1, 2, "POLISHED_GRANITE");
        variant(1, 3, "DIORITE"); variant(1, 4, "POLISHED_DIORITE"); variant(1, 5, "ANDESITE"); variant(1, 6, "POLISHED_ANDESITE");
        id(2, "GRASS");
        id(3, "DIRT");            variant(3, 1, "COARSE_DIRT");    variant(3, 2, "PODZOL");
        id(4, "COBBLESTONE");
        id(5, "WOOD");            variant(5, 1, "SPRUCE_PLANKS"); variant(5, 2, "BIRCH_PLANKS");
        variant(5, 3, "JUNGLE_PLANKS"); variant(5, 4, "ACACIA_PLANKS"); variant(5, 5, "DARK_OAK_PLANKS");
        id(6, "SAPLING");         variant(6, 1, "SPRUCE_SAPLING"); variant(6, 2, "BIRCH_SAPLING");
        variant(6, 3, "JUNGLE_SAPLING"); variant(6, 4, "ACACIA_SAPLING"); variant(6, 5, "DARK_OAK_SAPLING");
        id(7, "BEDROCK");
        id(8, "WATER");           id(9, "STATIONARY_WATER");
        id(10, "LAVA");           id(11, "STATIONARY_LAVA");
        id(12, "SAND");           variant(12, 1, "RED_SAND");
        id(13, "GRAVEL");
        id(14, "GOLD_ORE");       id(15, "IRON_ORE");             id(16, "COAL_ORE");
        id(17, "LOG");            variant(17, 1, "SPRUCE_LOG");    variant(17, 2, "BIRCH_LOG");     variant(17, 3, "JUNGLE_LOG");
        id(18, "LEAVES");         variant(18, 1, "SPRUCE_LEAVES"); variant(18, 2, "BIRCH_LEAVES");  variant(18, 3, "JUNGLE_LEAVES");
        id(19, "SPONGE");         variant(19, 1, "WET_SPONGE");
        id(20, "GLASS");
        id(21, "LAPIS_ORE");      id(22, "LAPIS_BLOCK");
        id(23, "DISPENSER");
        id(24, "SANDSTONE");      variant(24, 1, "CHISELED_SANDSTONE"); variant(24, 2, "SMOOTH_SANDSTONE");
        id(25, "NOTE_BLOCK");     id(26, "BED_BLOCK");
        id(27, "POWERED_RAIL");   id(28, "DETECTOR_RAIL");
        id(29, "PISTON_STICKY_BASE");
        id(30, "WEB");            id(31, "LONG_GRASS");           variant(31, 1, "GRASS");   variant(31, 2, "FERN");
        id(32, "DEAD_BUSH");      id(33, "PISTON_BASE");          id(34, "PISTON_EXTENSION");
        id(35, "WOOL");           variant(35, 0, "WHITE_WOOL");   variant(35, 1, "ORANGE_WOOL");   variant(35, 2, "MAGENTA_WOOL");
        variant(35, 3, "LIGHT_BLUE_WOOL"); variant(35, 4, "YELLOW_WOOL");  variant(35, 5, "LIME_WOOL");
        variant(35, 6, "PINK_WOOL");  variant(35, 7, "GRAY_WOOL");    variant(35, 8, "LIGHT_GRAY_WOOL");
        variant(35, 9, "CYAN_WOOL");  variant(35, 10, "PURPLE_WOOL"); variant(35, 11, "BLUE_WOOL");
        variant(35, 12, "BROWN_WOOL"); variant(35, 13, "GREEN_WOOL"); variant(35, 14, "RED_WOOL"); variant(35, 15, "BLACK_WOOL");
        id(37, "YELLOW_FLOWER");  id(38, "RED_ROSE");
        id(39, "BROWN_MUSHROOM"); id(40, "RED_MUSHROOM");
        id(41, "GOLD_BLOCK");     id(42, "IRON_BLOCK");
        id(43, "DOUBLE_STEP");    id(44, "STEP");
        id(45, "BRICK");          id(46, "TNT");
        id(47, "BOOKSHELF");      id(48, "MOSSY_COBBLESTONE");
        id(49, "OBSIDIAN");       id(50, "TORCH");
        id(51, "FIRE");           id(52, "MOB_SPAWNER");
        id(53, "WOOD_STAIRS");    id(54, "CHEST");
        id(55, "REDSTONE_WIRE");  id(56, "DIAMOND_ORE");
        id(57, "DIAMOND_BLOCK");  id(58, "WORKBENCH");
        id(59, "CROPS");          id(60, "SOIL");
        id(61, "FURNACE");        id(62, "BURNING_FURNACE");
        id(63, "SIGN_POST");      id(64, "WOOD_DOOR");
        id(65, "LADDER");         id(66, "RAILS");
        id(67, "COBBLESTONE_STAIRS"); id(68, "WALL_SIGN");
        id(69, "LEVER");          id(70, "STONE_PLATE");
        id(71, "IRON_DOOR_BLOCK"); id(72, "WOOD_PLATE");
        id(73, "REDSTONE_ORE");   id(74, "GLOWING_REDSTONE_ORE");
        id(75, "REDSTONE_TORCH_OFF"); id(76, "REDSTONE_TORCH_ON");
        id(77, "STONE_BUTTON");   id(78, "SNOW");
        id(79, "ICE");            id(80, "SNOW_BLOCK");
        id(81, "CACTUS");         id(82, "CLAY");
        id(83, "SUGAR_CANE_BLOCK"); id(84, "JUKEBOX");
        id(85, "FENCE");          id(86, "PUMPKIN");
        id(87, "NETHERRACK");     id(88, "SOUL_SAND");
        id(89, "GLOWSTONE");      id(90, "PORTAL");
        id(91, "JACK_O_LANTERN"); id(92, "CAKE_BLOCK");
        id(93, "DIODE_BLOCK_OFF"); id(94, "DIODE_BLOCK_ON");
        id(95, "STAINED_GLASS");  variant(95, 0, "WHITE_STAINED_GLASS"); variant(95, 1, "ORANGE_STAINED_GLASS");
        variant(95, 2, "MAGENTA_STAINED_GLASS"); variant(95, 3, "LIGHT_BLUE_STAINED_GLASS"); variant(95, 4, "YELLOW_STAINED_GLASS");
        variant(95, 5, "LIME_STAINED_GLASS"); variant(95, 6, "PINK_STAINED_GLASS"); variant(95, 7, "GRAY_STAINED_GLASS");
        variant(95, 8, "LIGHT_GRAY_STAINED_GLASS"); variant(95, 9, "CYAN_STAINED_GLASS"); variant(95, 10, "PURPLE_STAINED_GLASS");
        variant(95, 11, "BLUE_STAINED_GLASS"); variant(95, 12, "BROWN_STAINED_GLASS"); variant(95, 13, "GREEN_STAINED_GLASS");
        variant(95, 14, "RED_STAINED_GLASS"); variant(95, 15, "BLACK_STAINED_GLASS");
        id(96, "TRAP_DOOR");      id(97, "MONSTER_EGGS");
        id(98, "SMOOTH_BRICK");   variant(98, 1, "MOSSY_STONE_BRICKS"); variant(98, 2, "CRACKED_STONE_BRICKS"); variant(98, 3, "CHISELED_STONE_BRICKS");
        id(99, "HUGE_MUSHROOM_1"); id(100, "HUGE_MUSHROOM_2");
        id(101, "IRON_FENCE");    id(102, "THIN_GLASS");
        id(103, "MELON_BLOCK");   id(104, "PUMPKIN_STEM");
        id(105, "MELON_STEM");    id(106, "VINE");
        id(107, "FENCE_GATE");    id(108, "BRICK_STAIRS");
        id(109, "SMOOTH_STAIRS"); id(110, "MYCEL");
        id(111, "WATER_LILY");    id(112, "NETHER_BRICK");
        id(113, "NETHER_FENCE");  id(114, "NETHER_BRICK_STAIRS");
        id(115, "NETHER_WARTS");  id(116, "ENCHANTMENT_TABLE");
        id(117, "BREWING_STAND"); id(118, "CAULDRON");
        id(119, "ENDER_PORTAL");  id(120, "ENDER_PORTAL_FRAME");
        id(121, "ENDER_STONE");   id(122, "DRAGON_EGG");
        id(123, "REDSTONE_LAMP_OFF"); id(124, "REDSTONE_LAMP_ON");
        id(125, "WOOD_DOUBLE_STEP"); id(126, "WOOD_STEP");
        id(127, "COCOA");         id(128, "SANDSTONE_STAIRS");
        id(129, "EMERALD_ORE");   id(130, "ENDER_CHEST");
        id(131, "TRIPWIRE_HOOK"); id(132, "TRIPWIRE");
        id(133, "EMERALD_BLOCK"); id(134, "SPRUCE_WOOD_STAIRS");
        id(135, "BIRCH_WOOD_STAIRS"); id(136, "JUNGLE_WOOD_STAIRS");
        id(137, "COMMAND"); id(138, "BEACON");
        id(139, "COBBLE_WALL");   variant(139, 1, "MOSSY_COBBLESTONE_WALL");
        id(140, "FLOWER_POT");    id(141, "CARROT");
        id(142, "POTATO");        id(143, "WOOD_BUTTON");
        id(145, "ANVIL");         id(146, "TRAPPED_CHEST");
        id(147, "GOLD_PLATE");    id(148, "IRON_PLATE");
        id(149, "REDSTONE_COMPARATOR_OFF"); id(150, "REDSTONE_COMPARATOR_ON");
        id(151, "DAYLIGHT_DETECTOR"); id(152, "REDSTONE_BLOCK");
        id(153, "QUARTZ_ORE");    id(154, "HOPPER");
        id(155, "QUARTZ_BLOCK");  variant(155, 1, "CHISELED_QUARTZ_BLOCK"); variant(155, 2, "QUARTZ_PILLAR");
        id(156, "QUARTZ_STAIRS"); id(157, "ACTIVATOR_RAIL");
        id(158, "DROPPER");
        id(159, "STAINED_CLAY");  variant(159, 0, "WHITE_TERRACOTTA"); variant(159, 1, "ORANGE_TERRACOTTA");
        variant(159, 2, "MAGENTA_TERRACOTTA"); variant(159, 3, "LIGHT_BLUE_TERRACOTTA"); variant(159, 4, "YELLOW_TERRACOTTA");
        variant(159, 5, "LIME_TERRACOTTA"); variant(159, 6, "PINK_TERRACOTTA"); variant(159, 7, "GRAY_TERRACOTTA");
        variant(159, 8, "LIGHT_GRAY_TERRACOTTA"); variant(159, 9, "CYAN_TERRACOTTA"); variant(159, 10, "PURPLE_TERRACOTTA");
        variant(159, 11, "BLUE_TERRACOTTA"); variant(159, 12, "BROWN_TERRACOTTA"); variant(159, 13, "GREEN_TERRACOTTA");
        variant(159, 14, "RED_TERRACOTTA"); variant(159, 15, "BLACK_TERRACOTTA");
        id(160, "STAINED_GLASS_PANE"); variant(160, 0, "WHITE_STAINED_GLASS_PANE"); variant(160, 1, "ORANGE_STAINED_GLASS_PANE");
        variant(160, 2, "MAGENTA_STAINED_GLASS_PANE"); variant(160, 3, "LIGHT_BLUE_STAINED_GLASS_PANE"); variant(160, 4, "YELLOW_STAINED_GLASS_PANE");
        variant(160, 5, "LIME_STAINED_GLASS_PANE"); variant(160, 6, "PINK_STAINED_GLASS_PANE"); variant(160, 7, "GRAY_STAINED_GLASS_PANE");
        variant(160, 8, "LIGHT_GRAY_STAINED_GLASS_PANE"); variant(160, 9, "CYAN_STAINED_GLASS_PANE"); variant(160, 10, "PURPLE_STAINED_GLASS_PANE");
        variant(160, 11, "BLUE_STAINED_GLASS_PANE"); variant(160, 12, "BROWN_STAINED_GLASS_PANE"); variant(160, 13, "GREEN_STAINED_GLASS_PANE");
        variant(160, 14, "RED_STAINED_GLASS_PANE"); variant(160, 15, "BLACK_STAINED_GLASS_PANE");
        id(161, "LEAVES_2");      variant(161, 0, "ACACIA_LEAVES"); variant(161, 1, "DARK_OAK_LEAVES");
        id(162, "LOG_2");         variant(162, 0, "ACACIA_LOG"); variant(162, 1, "DARK_OAK_LOG");
        id(163, "ACACIA_STAIRS"); id(164, "DARK_OAK_STAIRS");
        id(165, "SLIME_BLOCK");   id(166, "BARRIER");
        id(167, "IRON_TRAPDOOR"); id(168, "PRISMARINE"); variant(168, 1, "PRISMARINE_BRICKS"); variant(168, 2, "DARK_PRISMARINE");
        id(169, "SEA_LANTERN");   id(170, "HAY_BLOCK");
        id(171, "CARPET");        variant(171, 0, "WHITE_CARPET"); variant(171, 1, "ORANGE_CARPET"); variant(171, 2, "MAGENTA_CARPET");
        variant(171, 3, "LIGHT_BLUE_CARPET"); variant(171, 4, "YELLOW_CARPET"); variant(171, 5, "LIME_CARPET");
        variant(171, 6, "PINK_CARPET"); variant(171, 7, "GRAY_CARPET"); variant(171, 8, "LIGHT_GRAY_CARPET");
        variant(171, 9, "CYAN_CARPET"); variant(171, 10, "PURPLE_CARPET"); variant(171, 11, "BLUE_CARPET");
        variant(171, 12, "BROWN_CARPET"); variant(171, 13, "GREEN_CARPET"); variant(171, 14, "RED_CARPET"); variant(171, 15, "BLACK_CARPET");
        id(172, "HARD_CLAY");     id(173, "COAL_BLOCK");
        id(174, "PACKED_ICE");
        id(175, "DOUBLE_PLANT");  variant(175, 0, "SUNFLOWER"); variant(175, 1, "LILAC"); variant(175, 2, "TALL_GRASS");
        variant(175, 3, "LARGE_FERN"); variant(175, 4, "ROSE_BUSH"); variant(175, 5, "PEONY");
        id(176, "STANDING_BANNER"); id(177, "WALL_BANNER");
        id(178, "DAYLIGHT_DETECTOR_INVERTED"); id(179, "RED_SANDSTONE");
        id(180, "RED_SANDSTONE_STAIRS"); id(181, "DOUBLE_STONE_SLAB2");
        id(182, "STONE_SLAB2");   id(183, "SPRUCE_FENCE_GATE");
        id(184, "BIRCH_FENCE_GATE"); id(185, "JUNGLE_FENCE_GATE");
        id(186, "DARK_OAK_FENCE_GATE"); id(187, "ACACIA_FENCE_GATE");
        id(188, "SPRUCE_FENCE");  id(189, "BIRCH_FENCE");
        id(190, "JUNGLE_FENCE");  id(191, "DARK_OAK_FENCE");
        id(192, "ACACIA_FENCE");  id(193, "SPRUCE_DOOR");
        id(194, "BIRCH_DOOR");    id(195, "JUNGLE_DOOR");
        id(196, "ACACIA_DOOR");   id(197, "DARK_OAK_DOOR");
        id(198, "END_ROD");       id(199, "CHORUS_PLANT");
        id(200, "CHORUS_FLOWER"); id(201, "PURPUR_BLOCK");
        id(202, "PURPUR_PILLAR"); id(203, "PURPUR_STAIRS");
        id(204, "PURPUR_DOUBLE_SLAB"); id(205, "PURPUR_SLAB");
        id(206, "END_BRICKS");    id(207, "BEETROOT_BLOCK");
        id(208, "GRASS_PATH");    id(209, "END_GATEWAY");
        id(210, "COMMAND_REPEATING"); id(211, "COMMAND_CHAIN");
        id(212, "FROSTED_ICE");   id(213, "MAGMA");
        id(214, "NETHER_WART_BLOCK"); id(215, "RED_NETHER_BRICK");
        id(216, "BONE_BLOCK");    id(217, "STRUCTURE_VOID");
        id(218, "OBSERVER");      id(219, "WHITE_SHULKER_BOX");
        id(234, "SHULKER_BOX");   id(235, "WHITE_GLAZED_TERRACOTTA");
        id(251, "CONCRETE");      variant(251, 0, "WHITE_CONCRETE"); variant(251, 1, "ORANGE_CONCRETE"); variant(251, 2, "MAGENTA_CONCRETE");
        variant(251, 3, "LIGHT_BLUE_CONCRETE"); variant(251, 4, "YELLOW_CONCRETE"); variant(251, 5, "LIME_CONCRETE");
        variant(251, 6, "PINK_CONCRETE"); variant(251, 7, "GRAY_CONCRETE"); variant(251, 8, "LIGHT_GRAY_CONCRETE");
        variant(251, 9, "CYAN_CONCRETE"); variant(251, 10, "PURPLE_CONCRETE"); variant(251, 11, "BLUE_CONCRETE");
        variant(251, 12, "BROWN_CONCRETE"); variant(251, 13, "GREEN_CONCRETE"); variant(251, 14, "RED_CONCRETE"); variant(251, 15, "BLACK_CONCRETE");
        id(252, "CONCRETE_POWDER"); variant(252, 0, "WHITE_CONCRETE_POWDER"); variant(252, 7, "GRAY_CONCRETE_POWDER");
        id(255, "STRUCTURE_BLOCK");

        // ---- Itens (256+) ----
        id(256, "IRON_SPADE");    id(257, "IRON_PICKAXE");   id(258, "IRON_AXE");
        id(259, "FLINT_AND_STEEL"); id(260, "APPLE");        id(261, "BOW");
        id(262, "ARROW");         id(263, "COAL");           variant(263, 1, "CHARCOAL");
        id(264, "DIAMOND");       id(265, "IRON_INGOT");     id(266, "GOLD_INGOT");
        id(267, "IRON_SWORD");    id(268, "WOOD_SWORD");     id(269, "WOOD_SPADE");
        id(270, "WOOD_PICKAXE");  id(271, "WOOD_AXE");       id(272, "STONE_SWORD");
        id(273, "STONE_SPADE");   id(274, "STONE_PICKAXE");  id(275, "STONE_AXE");
        id(276, "DIAMOND_SWORD"); id(277, "DIAMOND_SPADE");  id(278, "DIAMOND_PICKAXE");
        id(279, "DIAMOND_AXE");   id(280, "STICK");          id(281, "BOWL");
        id(282, "MUSHROOM_SOUP"); id(283, "GOLD_SWORD");     id(284, "GOLD_SPADE");
        id(285, "GOLD_PICKAXE");  id(286, "GOLD_AXE");       id(287, "STRING");
        id(288, "FEATHER");       id(289, "SULPHUR");        id(290, "WOOD_HOE");
        id(291, "STONE_HOE");     id(292, "IRON_HOE");       id(293, "DIAMOND_HOE");
        id(294, "GOLD_HOE");      id(295, "SEEDS");          id(296, "WHEAT");
        id(297, "BREAD");         id(298, "LEATHER_HELMET"); id(299, "LEATHER_CHESTPLATE");
        id(300, "LEATHER_LEGGINGS"); id(301, "LEATHER_BOOTS"); id(302, "CHAINMAIL_HELMET");
        id(303, "CHAINMAIL_CHESTPLATE"); id(304, "CHAINMAIL_LEGGINGS"); id(305, "CHAINMAIL_BOOTS");
        id(306, "IRON_HELMET");   id(307, "IRON_CHESTPLATE"); id(308, "IRON_LEGGINGS");
        id(309, "IRON_BOOTS");    id(310, "DIAMOND_HELMET"); id(311, "DIAMOND_CHESTPLATE");
        id(312, "DIAMOND_LEGGINGS"); id(313, "DIAMOND_BOOTS"); id(314, "GOLD_HELMET");
        id(315, "GOLD_CHESTPLATE"); id(316, "GOLD_LEGGINGS"); id(317, "GOLD_BOOTS");
        id(318, "FLINT");         id(319, "PORK");           id(320, "GRILLED_PORK");
        id(321, "PAINTING");      id(322, "GOLDEN_APPLE");   variant(322, 1, "ENCHANTED_GOLDEN_APPLE");
        id(323, "SIGN");          id(324, "WOOD_DOOR");      id(325, "BUCKET");
        id(326, "WATER_BUCKET");  id(327, "LAVA_BUCKET");    id(328, "MINECART");
        id(329, "SADDLE");        id(330, "IRON_DOOR");      id(331, "REDSTONE");
        id(332, "SNOW_BALL");     id(333, "BOAT");           id(334, "LEATHER");
        id(335, "MILK_BUCKET");   id(336, "CLAY_BRICK");     id(337, "CLAY_BALL");
        id(338, "SUGAR_CANE");    id(339, "PAPER");          id(340, "BOOK");
        id(341, "SLIME_BALL");    id(342, "STORAGE_MINECART"); id(343, "POWERED_MINECART");
        id(344, "EGG");           id(345, "COMPASS");        id(346, "FISHING_ROD");
        id(347, "WATCH");         id(348, "GLOWSTONE_DUST"); id(349, "RAW_FISH");
        variant(349, 1, "RAW_SALMON"); variant(349, 2, "CLOWNFISH"); variant(349, 3, "PUFFERFISH");
        id(350, "COOKED_FISH");   variant(350, 1, "COOKED_SALMON");
        id(351, "INK_SACK");
        variant(351, 0, "INK_SAC");     variant(351, 1, "ROSE_RED");    variant(351, 2, "CACTUS_GREEN");
        variant(351, 3, "COCOA_BEANS"); variant(351, 4, "LAPIS_LAZULI"); variant(351, 5, "PURPLE_DYE");
        variant(351, 6, "CYAN_DYE");    variant(351, 7, "LIGHT_GRAY_DYE"); variant(351, 8, "GRAY_DYE");
        variant(351, 9, "PINK_DYE");    variant(351, 10, "LIME_DYE");   variant(351, 11, "YELLOW_DYE");
        variant(351, 12, "LIGHT_BLUE_DYE"); variant(351, 13, "MAGENTA_DYE"); variant(351, 14, "ORANGE_DYE");
        variant(351, 15, "BONE_MEAL");
        id(352, "BONE");          id(353, "SUGAR");          id(354, "CAKE");
        id(355, "BED");           id(356, "DIODE");          id(357, "COOKIE");
        id(358, "MAP");           id(359, "SHEARS");         id(360, "MELON");
        id(361, "PUMPKIN_SEEDS"); id(362, "MELON_SEEDS");    id(363, "RAW_BEEF");
        id(364, "COOKED_BEEF");   id(365, "RAW_CHICKEN");    id(366, "COOKED_CHICKEN");
        id(367, "ROTTEN_FLESH");  id(368, "ENDER_PEARL");    id(369, "BLAZE_ROD");
        id(370, "GHAST_TEAR");    id(371, "GOLD_NUGGET");    id(372, "NETHER_STALK");
        id(373, "POTION");        id(374, "GLASS_BOTTLE");   id(375, "SPIDER_EYE");
        id(376, "FERMENTED_SPIDER_EYE"); id(377, "BLAZE_POWDER"); id(378, "MAGMA_CREAM");
        id(379, "BREWING_STAND_ITEM"); id(380, "CAULDRON_ITEM"); id(381, "EYE_OF_ENDER");
        id(382, "SPECKLED_MELON"); id(383, "MONSTER_EGG");   id(384, "EXP_BOTTLE");
        id(385, "FIREBALL");      id(386, "BOOK_AND_QUILL"); id(387, "WRITTEN_BOOK");
        id(388, "EMERALD");       id(389, "ITEM_FRAME");     id(390, "FLOWER_POT_ITEM");
        id(391, "CARROT_ITEM");   id(392, "POTATO_ITEM");    id(393, "BAKED_POTATO");
        id(394, "POISONOUS_POTATO"); id(395, "EMPTY_MAP");   id(396, "GOLDEN_CARROT");
        id(397, "SKULL_ITEM");
        variant(397, 0, "SKELETON_SKULL"); variant(397, 1, "WITHER_SKELETON_SKULL");
        variant(397, 2, "ZOMBIE_HEAD");    variant(397, 3, "PLAYER_HEAD");
        variant(397, 4, "CREEPER_HEAD");   variant(397, 5, "DRAGON_HEAD");
        id(398, "CARROT_STICK");  id(399, "NETHER_STAR");    id(400, "PUMPKIN_PIE");
        id(401, "FIREWORK");      id(402, "FIREWORK_CHARGE"); id(403, "ENCHANTED_BOOK");
        id(404, "DIODE");         id(405, "NETHER_BRICK_ITEM"); id(406, "QUARTZ");
        id(407, "EXPLOSIVE_MINECART"); id(408, "HOPPER_MINECART"); id(409, "PRISMARINE_SHARD");
        id(410, "PRISMARINE_CRYSTALS"); id(411, "RABBIT");    id(412, "COOKED_RABBIT");
        id(413, "RABBIT_STEW");   id(414, "RABBIT_FOOT");    id(415, "RABBIT_HIDE");
        id(416, "ARMOR_STAND");   id(417, "IRON_BARDING");   id(418, "GOLD_BARDING");
        id(419, "DIAMOND_BARDING"); id(420, "LEASH");        id(421, "NAME_TAG");
        id(422, "COMMAND_MINECART"); id(423, "MUTTON");      id(424, "COOKED_MUTTON");
        id(425, "BANNER");        id(427, "SPRUCE_DOOR_ITEM"); id(428, "BIRCH_DOOR_ITEM");
        id(429, "JUNGLE_DOOR_ITEM"); id(430, "ACACIA_DOOR_ITEM"); id(431, "DARK_OAK_DOOR_ITEM");
        id(432, "CHORUS_FRUIT");  id(433, "CHORUS_FRUIT_POPPED"); id(434, "BEETROOT");
        id(435, "BEETROOT_SEEDS"); id(436, "BEETROOT_SOUP"); id(437, "DRAGONS_BREATH");
        id(438, "SPLASH_POTION"); id(439, "SPECTRAL_ARROW"); id(440, "TIPPED_ARROW");
        id(441, "LINGERING_POTION"); id(442, "SHIELD");      id(443, "ELYTRA");
        id(444, "SPRUCE_BOAT");   id(445, "BIRCH_BOAT");     id(446, "JUNGLE_BOAT");
        id(447, "ACACIA_BOAT");   id(448, "DARK_OAK_BOAT");  id(449, "TOTEM");
        id(450, "SHULKER_SHELL"); id(452, "IRON_NUGGET");
        id(2256, "GOLD_RECORD");  id(2257, "GREEN_RECORD");
    }


    private static final Map<String, Color> COLOR_BY_NAME = new LinkedHashMap<>();

    static {
        for (Field f : Color.class.getDeclaredFields()) {
            if (f.getType() == Color.class) {
                try { COLOR_BY_NAME.put(f.getName().toUpperCase(), (Color) f.get(null)); }
                catch (IllegalAccessException ignored) {}
            }
        }
    }

    private BukkitUtils() {}


    public static String serializeItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "AIR : 1";

        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().name());
        if (item.getDurability() != 0) sb.append(":").append(item.getDurability());
        sb.append(" : ").append(item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return sb.toString();

        BookMeta             book      = meta instanceof BookMeta             ? (BookMeta)             meta : null;
        SkullMeta            skull     = meta instanceof SkullMeta            ? (SkullMeta)            meta : null;
        PotionMeta           potion    = meta instanceof PotionMeta           ? (PotionMeta)           meta : null;
        FireworkEffectMeta   effect    = meta instanceof FireworkEffectMeta   ? (FireworkEffectMeta)   meta : null;
        LeatherArmorMeta     armor     = meta instanceof LeatherArmorMeta     ? (LeatherArmorMeta)     meta : null;
        EnchantmentStorageMeta enchanted = meta instanceof EnchantmentStorageMeta ? (EnchantmentStorageMeta) meta : null;

        if (meta.hasDisplayName())
            sb.append(" : name>").append(decolor(meta.getDisplayName()));

        if (meta.hasLore()) {
            sb.append(" : desc>");
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++)
                sb.append(decolor(lore.get(i))).append(i + 1 < lore.size() ? "\n" : "");
        }

        Map<Enchantment, Integer> enchants = enchanted != null
                ? enchanted.getStoredEnchants()
                : meta.getEnchants();
        if (!enchants.isEmpty()) {
            sb.append(" : enchant>");
            int i = 0;
            for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                sb.append(e.getKey().getName()).append(":").append(e.getValue());
                if (++i < enchants.size()) sb.append("\n");
            }
        }

        if (skull != null && skull.hasOwner())
            sb.append(" : owner>").append(skull.getOwner());

        if (book != null && book.hasPages())
            sb.append(" : pages>").append(joinPages(book.getPages()));
        if (book != null && book.hasTitle())
            sb.append(" : title>").append(book.getTitle());
        if (book != null && book.hasAuthor())
            sb.append(" : author>").append(book.getAuthor());

        Color paintColor = null;
        if (armor  != null && armor.getColor()  != null) paintColor = armor.getColor();
        if (effect != null && effect.hasEffect() && !effect.getEffect().getColors().isEmpty())
            paintColor = effect.getEffect().getColors().get(0);
        if (paintColor != null)
            sb.append(" : paint>")
                    .append(paintColor.getRed()).append(":")
                    .append(paintColor.getGreen()).append(":")
                    .append(paintColor.getBlue());

        if (potion != null && potion.hasCustomEffects()) {
            sb.append(" : effect>");
            List<PotionEffect> effs = potion.getCustomEffects();
            for (int i = 0; i < effs.size(); i++) {
                PotionEffect pe = effs.get(i);
                sb.append(pe.getType().getName()).append(":")
                        .append(pe.getAmplifier()).append(":")
                        .append(pe.getDuration());
                if (i + 1 < effs.size()) sb.append("\n");
            }
        }

        for (ItemFlag flag : meta.getItemFlags())
            sb.append(" : hide>").append(flag.name());

        return decolor(sb.toString()).replace("\n", "\\n");
    }


    public static ItemStack deserializeItemStack(String raw) {
        if (raw == null || raw.isEmpty()) return new ItemStack(Material.AIR);

        raw = color(raw).replace("\\n", "\n");
        String[] tokens = raw.split(" : ");

        String matToken = tokens[0].trim();
        String[] matParts = matToken.split(":", 2);
        String matName = matParts[0].trim();
        short data = 0;
        if (matParts.length > 1) {
            try { data = Short.parseShort(matParts[1].trim()); }
            catch (NumberFormatException ignored) {}
        }
        Material mat = resolveMaterial(matName, data);

        int amount = 1;
        if (tokens.length > 1) {
            try { amount = Math.min(Integer.parseInt(tokens[1].trim()), 64); }
            catch (NumberFormatException ignored) {}
        }

        ItemStack item = new ItemStack(mat, amount);
        if (data != 0) {
            try { item.setDurability(data); }
            catch (Exception ignored) {}
        }

        if (tokens.length <= 2) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        BookMeta             book      = meta instanceof BookMeta             ? (BookMeta)             meta : null;
        SkullMeta            skull     = meta instanceof SkullMeta            ? (SkullMeta)            meta : null;
        PotionMeta           potion    = meta instanceof PotionMeta           ? (PotionMeta)           meta : null;
        FireworkEffectMeta   effect    = meta instanceof FireworkEffectMeta   ? (FireworkEffectMeta)   meta : null;
        LeatherArmorMeta     armor     = meta instanceof LeatherArmorMeta     ? (LeatherArmorMeta)     meta : null;
        EnchantmentStorageMeta enchanted = meta instanceof EnchantmentStorageMeta ? (EnchantmentStorageMeta) meta : null;

        List<String> lore = new ArrayList<String>();

        for (int i = 2; i < tokens.length; i++) {
            String opt = tokens[i].trim();

            if (opt.startsWith("name>")) {
                meta.setDisplayName(color(opt.substring(5)));

            } else if (opt.startsWith("desc>")) {
                for (String line : opt.substring(5).split("\n"))
                    lore.add(color(line));

            } else if (opt.startsWith("enchant>")) {
                for (String entry : opt.substring(8).split("\n")) {
                    String[] ep = entry.split(":", 2);
                    if (ep.length < 2) continue;
                    Enchantment ench = Enchantment.getByName(ep[0].toUpperCase());
                    if (ench == null) continue;
                    int level = parseIntSafe(ep[1], 1);
                    if (enchanted != null) enchanted.addStoredEnchant(ench, level, true);
                    else meta.addEnchant(ench, level, true);
                }

            } else if (opt.startsWith("paint>") && (armor != null || effect != null)) {
                String colorStr = opt.substring(6).trim();
                String[] rgb = colorStr.split(":");
                Color c = null;
                if (rgb.length == 3) {
                    c = Color.fromRGB(
                            parseIntSafe(rgb[0], 0),
                            parseIntSafe(rgb[1], 0),
                            parseIntSafe(rgb[2], 0)
                    );
                } else {
                    c = COLOR_BY_NAME.get(colorStr.toUpperCase());
                }
                if (c != null) {
                    if (armor  != null) armor.setColor(c);
                    if (effect != null) effect.setEffect(FireworkEffect.builder().withColor(c).build());
                }

            } else if (opt.startsWith("owner>") && skull != null) {
                skull.setOwner(opt.substring(6).trim());

            } else if (opt.startsWith("skin>") && skull != null) {
                String texture = opt.substring(5).trim();
                if (!texture.isEmpty()) setSkullTexture(meta, texture);

            } else if (opt.startsWith("pages>") && book != null) {
                book.setPages(opt.substring(6).split("\\{pular}"));

            } else if (opt.startsWith("title>") && book != null) {
                book.setTitle(opt.substring(6));

            } else if (opt.startsWith("author>") && book != null) {
                book.setAuthor(opt.substring(7));

            } else if (opt.startsWith("effect>") && potion != null) {
                for (String entry : opt.substring(7).split("\n")) {
                    String[] ep = entry.split(":");
                    if (ep.length < 3) continue;
                    PotionEffectType type = PotionEffectType.getByName(ep[0]);
                    if (type == null) continue;
                    potion.addCustomEffect(
                            new PotionEffect(type, parseIntSafe(ep[2], 200), parseIntSafe(ep[1], 0)),
                            false
                    );
                }

            } else if (opt.startsWith("hide>")) {
                String flagStr = opt.substring(5).trim();
                if (flagStr.equalsIgnoreCase("all")) {
                    meta.addItemFlags(ItemFlag.values());
                } else {
                    for (String f : flagStr.split("\n")) {
                        try { meta.addItemFlags(ItemFlag.valueOf(f.trim().toUpperCase())); }
                        catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }

        if (!lore.isEmpty()) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack putProfileOnSkull(Player player, ItemStack head) {
        if (head == null || !(head.getItemMeta() instanceof SkullMeta)) return head;
        ItemMeta meta = head.getItemMeta();
        try {
            Method m = SkullMeta.class.getMethod("setOwningPlayer", org.bukkit.OfflinePlayer.class);
            m.invoke(meta, player);
        } catch (NoSuchMethodException ignored) {
            try {
                SkullMeta sm = (SkullMeta) meta;
                sm.setOwner(player.getName());
            } catch (Exception e) {
                LOGGER.warning("[BukkitUtils] Erro ao definir skull owner: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.warning("[BukkitUtils] Erro ao definir skull por player: " + e.getMessage());
        }
        head.setItemMeta(meta);
        return head;
    }


    public static ItemStack putTextureOnSkull(String textureBase64, ItemStack head) {
        if (head == null || !(head.getItemMeta() instanceof SkullMeta)) return head;
        ItemMeta meta = head.getItemMeta();
        setSkullTexture(meta, textureBase64);
        head.setItemMeta(meta);
        return head;
    }

    public static void putGlowEnchantment(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        Enchantment lure = Enchantment.getByName("LURE");
        if (lure == null) lure = Enchantment.LURE;
        meta.addEnchant(lure, 1, true);
        try { meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); }
        catch (Exception ignored) {}
        item.setItemMeta(meta);
    }


    public static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "; "
                + loc.getX()   + "; " + loc.getY()     + "; " + loc.getZ() + "; "
                + loc.getYaw() + "; " + loc.getPitch();
    }

    public static Location deserializeLocation(String serialized) {
        String[] p = serialized.split("; ");
        Location loc = new Location(
                Bukkit.getWorld(p[0]),
                Double.parseDouble(p[1]),
                Double.parseDouble(p[2]),
                Double.parseDouble(p[3])
        );
        loc.setYaw(Float.parseFloat(p[4]));
        loc.setPitch(Float.parseFloat(p[5]));
        return loc;
    }


    public static void displayParticle(Player viewer,
                                       String particleName,
                                       boolean isFar,
                                       float x, float y, float z,
                                       float offX, float offY, float offZ,
                                       float speed, int count) {
        if (IS_1_9_PLUS) {
            displayParticleAPI(viewer, particleName, x, y, z, offX, offY, offZ, speed, count);
        } else {
            displayParticleNMS18(viewer, particleName, isFar, x, y, z, offX, offY, offZ, speed, count);
        }
    }


    private static Material resolveMaterial(String name, short data) {
        String upper = name.toUpperCase();

        // Config antiga guardou o item como ID numérico puro (ex: "404", "351:8").
        // Traduz usando a tabela NUMERIC_LEGACY_ID / NUMERIC_LEGACY_VARIANTS.
        if (isNumericId(upper)) {
            Material fromId = resolveNumericId(Integer.parseInt(upper), data);
            if (fromId != null) return fromId;
        }

        try { return Material.valueOf(upper); }
        catch (IllegalArgumentException ignored) {}

        String modern = LEGACY_ALIAS.get(upper);
        if (modern != null) {
            try { return Material.valueOf(modern); }
            catch (IllegalArgumentException ignored) {}
        }


        if (modern != null) {
            Material m = Material.matchMaterial(modern);
            if (m != null) return m;
        }

        Material m = Material.matchMaterial(upper);
        if (m != null) return m;

        LOGGER.warning("[BukkitUtils] Material desconhecido: " + name
                + (data > 0 ? ":" + data : "") + " → usando STONE");
        return Material.STONE;
    }

    private static boolean isNumericId(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Traduz um ID numérico clássico (1-452, 2256/2257) + data value pro nome
     * legado correspondente (usando NUMERIC_LEGACY_VARIANTS quando o data value
     * importa, senão NUMERIC_LEGACY_ID) e então resolve esse nome legado pro
     * Material moderno de verdade, passando pela mesma cadeia de aliases usada
     * pros nomes de texto (o nome guardado na tabela às vezes ainda é um nome
     * antigo tipo "DIODE" ou "SKULL_ITEM", que também precisa de alias).
     */
    private static Material resolveNumericId(int id, short data) {
        String legacyName = null;

        Map<Short, String> variants = NUMERIC_LEGACY_VARIANTS.get(id);
        if (variants != null) legacyName = variants.get(data);

        if (legacyName == null) legacyName = NUMERIC_LEGACY_ID.get(id);
        if (legacyName == null) return null;

        try { return Material.valueOf(legacyName); }
        catch (IllegalArgumentException ignored) {}

        String modern = LEGACY_ALIAS.get(legacyName);
        if (modern != null) {
            try { return Material.valueOf(modern); }
            catch (IllegalArgumentException ignored) {}

            Material m = Material.matchMaterial(modern);
            if (m != null) return m;
        }

        return Material.matchMaterial(legacyName);
    }


    private static void setSkullTexture(ItemMeta meta, String textureBase64) {
        try {
            ClassLoader cl = Bukkit.getServer().getClass().getClassLoader();
            Class<?> gameProfileClass = cl.loadClass("com.mojang.authlib.GameProfile");
            Class<?> propertyClass    = cl.loadClass("com.mojang.authlib.properties.Property");

            Object profile = gameProfileClass
                    .getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "Head");

            Object propertyMap = gameProfileClass.getMethod("getProperties").invoke(profile);

            Object property = propertyClass
                    .getConstructor(String.class, String.class)
                    .newInstance("textures", textureBase64);

            propertyMap.getClass()
                    .getMethod("put", Object.class, Object.class)
                    .invoke(propertyMap, "textures", property);

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);

        } catch (Exception e) {
            LOGGER.warning("[BukkitUtils] Não foi possível definir textura do skull: " + e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void displayParticleAPI(Player viewer,
                                           String particleName,
                                           float x, float y, float z,
                                           float offX, float offY, float offZ,
                                           float speed, int count) {
        try {
            Class<?> particleEnum = Class.forName("org.bukkit.Particle");
            String name = normalizeParticle19(particleName);
            Object particle = Enum.valueOf((Class<Enum>) particleEnum, name);

            Method m = Player.class.getMethod(
                    "spawnParticle", particleEnum,
                    double.class, double.class, double.class,
                    int.class,
                    double.class, double.class, double.class,
                    double.class
            );
            m.invoke(viewer, particle,
                    (double) x, (double) y, (double) z,
                    count,
                    (double) offX, (double) offY, (double) offZ,
                    (double) speed);
        } catch (Exception e) {
            LOGGER.warning("[BukkitUtils] Erro ao spawnar partícula (1.9+ API) '" + particleName + "': " + e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void displayParticleNMS18(Player viewer,
                                             String particleName,
                                             boolean isFar,
                                             float x, float y, float z,
                                             float offX, float offY, float offZ,
                                             float speed, int count) {
        try {
            String pkg = "net.minecraft.server." + NMS_VERSION + ".";
            Class<?> enumParticle = Class.forName(pkg + "EnumParticle");
            Class<?> packetClass  = Class.forName(pkg + "PacketPlayOutWorldParticles");

            String name = normalizeParticle18(particleName, enumParticle);
            Object particle = Enum.valueOf((Class<Enum>) enumParticle, name);

            Object packet = packetClass.getConstructor(
                    enumParticle, boolean.class,
                    float.class, float.class, float.class,
                    float.class, float.class, float.class,
                    float.class, int.class, int[].class
            ).newInstance(particle, isFar, x, y, z, offX, offY, offZ, speed, count, new int[0]);

            Object nmsPlayer = viewer.getClass().getMethod("getHandle").invoke(viewer);
            Object conn      = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Class<?> packetItf = Class.forName(pkg + "Packet");
            conn.getClass().getMethod("sendPacket", packetItf).invoke(conn, packet);

        } catch (Exception e) {
            LOGGER.warning("[BukkitUtils] Erro ao spawnar partícula (NMS 1.8) '" + particleName + "': " + e.getMessage());
        }
    }

    private static String normalizeParticle19(String name) {
        String up = name.toUpperCase().replace(" ", "_");
        if (up.equals("HAPPYVILLAGER")) return "VILLAGER_HAPPY";
        if (up.equals("ANGRYVILLAGER")) return "VILLAGER_ANGRY";
        return up;
    }

    @SuppressWarnings("rawtypes")
    private static String normalizeParticle18(String name, Class<?> enumParticle) {
        String up = name.toUpperCase().replace(" ", "_");
        for (Object c : enumParticle.getEnumConstants())
            if (((Enum<?>) c).name().equals(up)) return up;
        String flat = up.replace("_", "");
        for (Object c : enumParticle.getEnumConstants())
            if (((Enum<?>) c).name().replace("_", "").equalsIgnoreCase(flat))
                return ((Enum<?>) c).name();
        return up;
    }

    private static String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }

    private static String decolor(String s) {
        return s == null ? "" : s.replace("§", "&");
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private static String joinPages(List<String> pages) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            sb.append(pages.get(i));
            if (i + 1 < pages.size()) sb.append("{pular}");
        }
        return sb.toString();
    }
}