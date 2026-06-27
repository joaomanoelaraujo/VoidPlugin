package org.ltzin.player.enums;

public enum TimeOfDay {

    DIA,
    TARDE,
    NOITE;

    private static final TimeOfDay[] VALUES = values();

    public static TimeOfDay getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return DIA;
    }

    public String getInkSack() {
        switch (this) {
            case DIA:   return "4";
            case TARDE: return "1";
            case NOITE: return "8";
            default:    return "4";
        }
    }

    public String getName() {
        switch (this) {
            case DIA:   return "§eDia";
            case TARDE: return "§6Tarde";
            case NOITE: return "§9Noite";
            default:    return "§eDia";
        }
    }

    public String getColor() {
        switch (this) {
            case DIA:   return "§e";
            case TARDE: return "§6";
            case NOITE: return "§9";
            default:    return "§e";
        }
    }

    public long getTime() {
        switch (this) {
            case DIA:   return 6000L;
            case TARDE: return 12000L;
            case NOITE: return 18000L;
            default:    return 6000L;
        }
    }

    public TimeOfDay next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}