package org.ltzin.menus.category;

public enum MenuCategory {

    INGAME("Ingame"),
    CHAT("Chat"),
    SOCIALS("Socials"),
    LOBBY("Lobby"),
    GUILD("Guild");

    private String value;

    MenuCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

}
