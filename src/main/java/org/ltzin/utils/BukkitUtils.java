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