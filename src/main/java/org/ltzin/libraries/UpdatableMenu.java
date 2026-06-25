package org.ltzin.libraries;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class UpdatableMenu extends Menu implements Listener {

    private BukkitTask task;

    public UpdatableMenu(String title) {
        this(title, 3);
    }

    public UpdatableMenu(String title, int rows) {
        super(title, rows);
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

    public void open(org.bukkit.entity.Player player) {
        player.openInventory(getInventory());
    }

    public abstract void update();
}