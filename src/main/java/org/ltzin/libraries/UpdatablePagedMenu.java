package org.ltzin.libraries;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public abstract class UpdatablePagedMenu extends PagedMenu implements Listener {

    private BukkitTask task;

    public UpdatablePagedMenu(String name) {
        this(name, 3);
    }

    public UpdatablePagedMenu(String name, int rows) {
        super(name, rows);
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