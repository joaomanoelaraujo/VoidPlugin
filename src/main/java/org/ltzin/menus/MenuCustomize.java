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
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;


public class MenuCustomize extends PlayerMenu {

    public MenuCustomize(Profile profile) {
        super(profile.getPlayer(), "Aparência", 6);

        this.setItem(12, BukkitUtils.deserializeItemStack(
                "CHEST : 1 : name>§aSkins utilizadas : desc>§7Aqui você pode acessar as últimas\n" +
                "§7sete skins que utilizou.\n\n" +
                "§a➜ §aClique aqui para visualizar."));

        this.setItem(14, BukkitUtils.deserializeItemStack(
                "BOOKSHELF : 1 : name>§aGaleria de skins : desc>§7Desbloqueie novas skins super estilosas\n" +
                "§7gratuitamente ao subir de nível ou\n" +
                "§7através de caixas misteriosas.\n\n" +
                "§a➜ §aClique aqui para visualizar."));

        this.setItem(29, BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>§bInformações : desc>§fSkin em uso: §7" + skinEmUsoLabel(profile) + "\n" +
                "§fOrigem: §7Sua conta\n\n" +
                "§fFormato: §bClássico\n" +
                "§fCapa: §aSim : skin>" + skinInformacoes()));

        this.setItem(30, BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>§aPesquisar : desc>§7Procure uma nova skin utilizando os\n" +
                "§7nicknames de outros jogadores para\n" +
                "§7aplicá-la em sua conta.\n\n" +
                "§a➜ §aClique aqui para pesquisar. : skin>" + skinPesquisar()));

        this.setItem(33, BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>§aAjuda : desc>§7Algumas ações neste menu também\n" +
                "§7podem ser realizadas por comando.\n\n" +
                "§e➜ §eClique para listar os comandos. : skin>" + skinAjuda()));

        this.setItem(34, BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>§dRedefinir : desc>§7Redefina a sua skin para a última\n" +
                "§7utilizada em sua conta Minecraft.\n\n" +
                "§7Caso sua conta não seja premium, a\n" +
                "§7skin será padronizada.\n\n" +
                "§a➜ §aClique aqui para redefinir. : skin>" + skinRedefinir()));

        this.setItem(49, BukkitUtils.deserializeItemStack(
                "ARROW : 1 : name>§cVoltar : desc>§7Clique para voltar."));

        this.register(Main.getInstance());
        this.open();
    }


    private String skinInformacoes() { return ""; }
    private String skinPesquisar()   { return ""; }
    private String skinAjuda()       { return ""; }
    private String skinRedefinir()   { return ""; }

    private String skinEmUsoLabel(Profile profile) {
        return "Nenhuma";
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
                        switch (evt.getSlot()) {
                            case 12:
                                break;

                            case 14:
                                this.player.closeInventory();
                                new MenuSkinGallery(profile);
                                break;

                            case 29:
                                break;

                            case 30:
                                break;

                            case 33:
                                break;

                            case 34:
                                break;

                            case 49:
                                this.player.closeInventory();
                                new MenuProfile(profile);
                                break;

                            default:
                                break;
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