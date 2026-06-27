package org.ltzin.player.enums;

public enum AutoQueue {

    TODOS,
    NENHUM;

    private static final AutoQueue[] VALUES = values();

    public static AutoQueue getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return NENHUM;
    }

    public String getInkSack() { return this == TODOS ? "10" : "8"; }
    public String getName()    { return this == TODOS ? "§aON" : "§cOFF"; }
    public String getColor()   { return this == TODOS ? "§a" : "§c"; }

    public AutoQueue next() { return this == TODOS ? NENHUM : TODOS; }
}