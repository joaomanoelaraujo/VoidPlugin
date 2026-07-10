package org.ltzin.utils;

import org.bukkit.Bukkit;

public final class VersionUtil {

    private static final int MINOR_VERSION;

    static {
        MINOR_VERSION = parseMinor();
    }

    private VersionUtil() {}

    private static int parseMinor() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        try {
            String[] parts = bukkitVersion.split("-")[0].split("\\.");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String nmsVersion = pkg.substring(pkg.lastIndexOf('.') + 1); // v1_8_R3
            try {
                String[] parts = nmsVersion.replace("v", "").split("_");
                return Integer.parseInt(parts[1]);
            } catch (Exception ex) {
                return 8; // assume o mínimo suportado
            }
        }
    }

    public static int getMinor() {
        return MINOR_VERSION;
    }

    public static boolean isAtLeast(int minor) {
        return MINOR_VERSION >= minor;
    }

    public static boolean supportsTextDisplay() {
        return isAtLeast(20) || (isAtLeast(19) && Bukkit.getBukkitVersion().contains("1.19.4"));
    }

    public static boolean supportsArmorStandMarkerMethod() {
        return isAtLeast(16);
    }

    public static boolean isLegacy() {
        return MINOR_VERSION < 13;
    }
}
