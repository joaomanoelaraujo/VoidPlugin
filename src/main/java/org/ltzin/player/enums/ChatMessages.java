package org.ltzin.player.enums;

public enum ChatMessages {

    TODOS,
    NENHUM;

    private static final ChatMessages[] VALUES = values();

    public static ChatMessages getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return TODOS;
    }

    public String getInkSack() { return this == TODOS ? "10" : "8"; }
    public String getName()    { return this == TODOS ? "§aON" : "§cOFF"; }
    public String getColor()   { return this == TODOS ? "§a" : "§c"; }

    public ChatMessages next() { return this == TODOS ? NENHUM : TODOS; }
}