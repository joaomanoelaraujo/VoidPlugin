package org.ltzin.tab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ltzin.Main;


public final class TabListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!player.isOnline()) return;
            TabManager.applyAllSync(player);

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
            }
        }, 1L);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent evt) {
        String name = evt.getPlayer().getName();
        TabManager.removeFromTeams(name);
    }
}