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
import org.ltzin.menus.category.MenuCategory;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class MenuProfile extends PlayerMenu {

    private static final SimpleDateFormat SDF_PT =
            new SimpleDateFormat("dd/MM/yyyy - HH:mm", new Locale("pt", "BR"));

    public MenuProfile(Profile profile) {
        super(profile.getPlayer(), "Perfil", 3);

        this.setItem(10, BukkitUtils.deserializeItemStack("404 : 1 : name>§aPreferências : desc>§7Controle diversas preferências\n§7pessoais da nossa rede.\n\n§eClique para ver!"));
        this.setItem(11, BukkitUtils.deserializeItemStack("PAPER : 1 : name>§aEstatísticas : desc>§7Veja todas as suas estatísticas\n§7de todos os nossos minigames.\n\n§eClique para ver!"));

        this.setItem(13, BukkitUtils.putProfileOnSkull(this.player, BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : name>§7" + profile.getName() + " : desc>§fGrupo: §7" + profile.getRole() + "\n\n§fCadastrado em: §7" + SDF_PT.format(profile.getCreated()) + "\n§fÚltimo login: §7" + SDF_PT.format(profile.getLastLogin()) + "\n\n§fEmail: §7Nenhum")));

        this.setItem(15, BukkitUtils.deserializeItemStack("342 : 1 : name>§aEntregas : desc>§7Veja suas recompensas disponíveis\n§7em nosso servidor!\n\n §7Recompensas Disponíveis:\n  §f▪ Nenhuma\n\n§eClique para ver!"));
        this.setItem(16, BukkitUtils.deserializeItemStack("SKULL_ITEM:3 : 1 : name>§aCustomização : desc>§7Customize sua conta da maneira\n§7que achar melhor!\n\n §7Atualmente Disponível:\n  §f▪ Aparência\n\n§eClique para ver! : skin>" + value()));

//        this.setItem(0, BukkitUtils.deserializeItemStack("DIAMOND_SWORD : 1 : name>§eTeste : desc>§8Ao clicar aqui você estará\n§8testando a biblioteca do propio\n§8VoidlessPlugin\n\n§eClique para testar! : hide>all"));

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
                        if (evt.getSlot() == 10) {
                            new MenuPreferences(profile, MenuCategory.INGAME);
                        } else if (evt.getSlot() == 16) {
                            new MenuCustomize(profile);
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
