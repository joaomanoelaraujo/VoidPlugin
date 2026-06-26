package org.ltzin.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.ltzin.Main;
import org.ltzin.libraries.PlayerMenu;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class MenuProfile extends PlayerMenu {

    private static final SimpleDateFormat SDF_PT = new SimpleDateFormat("d 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR"));

    public MenuProfile(Profile profile) {
        super(profile.getPlayer(), "Perfil", 4);

        this.setItem(13, BukkitUtils.putProfileOnSkull(this.player, BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : name>§aSuas informações : desc>§fRank: " + profile.getRole() + "\n§fCash: §b" + profile.getCash() + "\n§fGuilda: §7Nenhuma" + "\n\n§fCadastrado: §e" + SDF_PT.format(profile.getCreated()) + "\n§fUltimo login: §c" + SDF_PT.format(profile.getLastLogin()))));

//        this.setItem(0, BukkitUtils.deserializeItemStack("DIAMOND_SWORD : 1 : name>§eTeste : desc>§8Ao clicar aqui você estará\n§8testando a biblioteca do propio\n§8VoidlessPlugin\n\n§eClique para testar! : hide>all"));

        this.register(Main.getInstance());
        this.open();
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
                        if (evt.getSlot() == 0) {
                            player.closeInventory();
                            player.sendMessage("§eTestando 1....2....3....");
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
