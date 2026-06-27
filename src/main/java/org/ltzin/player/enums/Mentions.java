package org.ltzin.player.enums;

public enum Mentions {

    TODOS,
    NENHUM;

    private static final Mentions[] VALUES = values();

    public static Mentions getByOrdinal(long ordinal) {
        if (ordinal > -1 && ordinal < VALUES.length) return VALUES[(int) ordinal];
        return TODOS;
    }

    public String getInkSack() { return this == TODOS ? "10" : "8"; }
    public String getName()    { return this == TODOS ? "§aON" : "§cOFF"; }
    public String getColor()   { return this == TODOS ? "§a" : "§c"; }

    public Mentions next() { return this == TODOS ? NENHUM : TODOS; }
}