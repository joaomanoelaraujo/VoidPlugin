package org.ltzin.menus;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.ltzin.Main;
import org.ltzin.libraries.PlayerMenu;
import org.ltzin.menus.category.MenuCategory;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;
import org.ltzin.utils.EnumSound;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class MenuStats extends PlayerMenu {


    public MenuStats(Profile profile) {
        super(profile.getPlayer(), "Estatísticas", 4);

        this.setItem(12, BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : name>§aSky Wars : desc> §eSolo:\n  §fAbates: §70\n  §fMortes: §70\n  §fVitórias: §70\n  §fPartidas: §70\n  §fAssistências: §70\n\n §eDupla:\n  §fAbates: §70\n  §fMortes: §70\n  §fVitórias: §70\n  §fPartidas: §70\n  §fAssistências: §70\n\n§fCoins: §60 : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlmN2EwNGFjMzM0ZmNhZjYxOGRhOWU4NDFmMDNjMDBkNzQ5MDAyZGM1OTJmODU0MGVmOTUzNDQ0MmNlY2Y0MiJ9fX0="));
        this.setItem(14, BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : name>§aBed Wars : desc> §eSolo:\n  §fAbates Finais: §70\n  §fMortes: §70\n  §fVitórias: §70\n  §fPartidas: §70\n  §fCamas Destr.: §70\n\n §eDupla:\n  §fAbates Finais: §70\n  §fMortes: §70\n  §fVitórias: §70\n  §fPartidas: §70\n  §fCamas Destr.: §70\n\n§fCoins: §60 : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmZiMjkwYTEzZGY4ODI2N2VhNWY1ZmNmNzk2YjYxNTdmZjY0Y2NlZTVjZDM5ZDQ2OTcyNDU5MWJhYmVlZDFmNiJ9fX0="));

        this.setItem(31, BukkitUtils.deserializeItemStack("ARROW : 1 : name>§cFechar : desc>§7Voltar ao perfil."));
        this.register(Main.getInstance());
        this.open();
    }


    public String value() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQxMTJjOTVmZTVjYzc0OGYwZWM1Mzk3ODE4OTQ3NzhmOGIzYWJjMzRhOTE4MGQ1ZTRmMGUxOTM2ZjdjN2E4YyJ9fX0=";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getInventory().equals(this.getInventory())) {
            evt.setCancelled(true);

            if (evt.getWhoClicked().equals(this.player)) {
                Profile profile = Profile.getProfile(this.player.getName());
                if (profile == null) {
                    this.player.closeInventory();
                    return;
                }

                if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getInventory())) {
                    ItemStack item = evt.getCurrentItem();

                    if (item != null && item.getType() != Material.AIR) {
                        if (evt.getSlot() == 31) {
                            EnumSound.CLICK.play(this.player, 1.0F, 2.0F);
                            new MenuProfile(profile);
                        }
                    }
                }
            }
        }
    }

    public void cancel() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        if (evt.getPlayer().equals(this.player)) {
            this.cancel();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getInventory())) {
            this.cancel();
        }
    }

}
