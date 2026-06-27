package org.ltzin.player.enums;

public enum GuildChat {

    TODOS,
    NENHUM;

    private static final GuildChat[] VALUES = values();

    public static GuildChat getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return TODOS;
    }

    public String getInkSack() { return this == TODOS ? "10" : "8"; }
    public String getName()    { return this == TODOS ? "§aON" : "§cOFF"; }
    public String getColor()   { return this == TODOS ? "§a" : "§c"; }

    public GuildChat next() { return this == TODOS ? NENHUM : TODOS; }
}