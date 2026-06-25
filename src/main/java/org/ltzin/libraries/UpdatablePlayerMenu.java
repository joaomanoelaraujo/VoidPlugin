package org.ltzin.libraries;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;


public abstract class UpdatablePlayerMenu extends UpdatableMenu implements Listener {

    protected Player player;

    public UpdatablePlayerMenu(Player player, String title) {
        this(player, title, 3);
    }

    public UpdatablePlayerMenu(Player player, String title, int rows) {
        super(title, rows);
        this.player = player;
    }

    public void open() {
        player.openInventory(getInventory());
    }

    public Player getPlayer() {
        return player;
    }
}