package org.ltzin.libraries;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PlayerMenu extends Menu implements Listener {

    protected Player player;

    public PlayerMenu(Player player, String title) {
        this(player, title, 3);
    }

    public PlayerMenu(Player player, String title, int rows) {
        super(title, rows);
        this.player = player;
    }

    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        this.player.openInventory(getInventory());
    }

    public Player getPlayer() {
        return player;
    }
}