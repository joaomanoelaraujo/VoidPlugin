package org.ltzin.libraries;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public abstract class UpdatablePlayerPagedMenu extends PagedPlayerMenu implements Listener {

    private BukkitTask task;

    public UpdatablePlayerPagedMenu(org.bukkit.entity.Player player, String name) {
        this(player, name, 3);
    }

    public UpdatablePlayerPagedMenu(org.bukkit.entity.Player player, String name, int rows) {
        super(player, name, rows);
    }


    public void register(Plugin plugin, long updateEveryTicks) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(plugin, 0L, updateEveryTicks);
    }

    public void cancel() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    public abstract void update();
}