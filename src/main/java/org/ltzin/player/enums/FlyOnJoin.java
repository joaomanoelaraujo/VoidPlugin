package org.ltzin.player.enums;

public enum FlyOnJoin {

    TODOS,
    NENHUM;

    private static final FlyOnJoin[] VALUES = values();

    public static FlyOnJoin getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return NENHUM;
    }

    public String getInkSack() { return this == TODOS ? "10" : "8"; }
    public String getName()    { return this == TODOS ? "§aON" : "§cOFF"; }
    public String getColor()   { return this == TODOS ? "§a" : "§c"; }

    public FlyOnJoin next() { return this == TODOS ? NENHUM : TODOS; }
}