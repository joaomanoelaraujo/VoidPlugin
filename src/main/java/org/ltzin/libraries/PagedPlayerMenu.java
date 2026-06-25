package org.ltzin.libraries;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PagedPlayerMenu extends PagedMenu implements Listener {

    protected Player player;
    protected int currentPage = 1;

    public PagedPlayerMenu(Player player, String name) {
        this(player, name, 3);
    }

    public PagedPlayerMenu(Player player, String name, int rows) {
        super(name, rows);
        this.player = player;
    }


    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    public void open() {
        if (pages.isEmpty()) return;
        currentPage = 1;
        player.openInventory(pages.get(0).getInventory());
    }


    public void openPrevious() {
        if (currentPage <= 1) return;
        currentPage--;
        player.openInventory(pages.get(currentPage - 1).getInventory());
    }

    public void openNext() {
        if (currentPage >= pages.size()) return;
        currentPage++;
        player.openInventory(pages.get(currentPage - 1).getInventory());
    }

    public Player getPlayer()   { return player; }
    public int getCurrentPage() { return currentPage; }
}