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
import org.ltzin.listeners.VoidlessListeners;
import org.ltzin.player.Profile;
import org.ltzin.skin.SkinData;
import org.ltzin.skin.SkinLibrary;
import org.ltzin.skin.SkinPreset;
import org.ltzin.utils.BukkitUtils;

import java.util.List;


public class MenuSkinGallery extends PlayerMenu {

    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int PAGE_SIZE = GRID_SLOTS.length;

    private static final int SLOT_VOLTAR    = 45;
    private static final int SLOT_ANTERIOR  = 48;
    private static final int SLOT_INDICADOR = 49;
    private static final int SLOT_PROXIMA   = 50;

    private final Profile profile;
    private int page = 0;

    public MenuSkinGallery(Profile profile) {
        super(profile.getPlayer(), "Galeria de skins", 6);
        this.profile = profile;

        this.register(Main.getInstance());
        renderPage();
        this.open();
    }


    private void renderPage() {
        List<SkinPreset> presets = SkinLibrary.getAll();
        int totalPages = Math.max(1, (int) Math.ceil(presets.size() / (double) PAGE_SIZE));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int start = page * PAGE_SIZE;

        for (int i = 0; i < GRID_SLOTS.length; i++) {
            int slot = GRID_SLOTS[i];
            int index = start + i;

            if (index < presets.size()) {
                SkinPreset preset = presets.get(index);
                this.setItem(slot, buildSkinItem(preset));
            } else {
                this.setItem(slot, new ItemStack(Material.AIR));
            }
        }

        this.setItem(SLOT_VOLTAR, BukkitUtils.deserializeItemStack(
                "BONE : 1 : name>§cVoltar : desc>§7Clique para retornar ao menu anterior."));

        boolean temAnterior = page > 0;
        boolean temProxima  = page + 1 < totalPages;

        this.setItem(SLOT_ANTERIOR, BukkitUtils.deserializeItemStack(
                temAnterior
                        ? "ARROW : 1 : name>§ePágina anterior : desc>§7Clique para voltar uma página."
                        : "ARROW : 1 : name>§7Página anterior : desc>§7Você já está na primeira página."));

        this.setItem(SLOT_INDICADOR, BukkitUtils.deserializeItemStack(
                "PAPER : 1 : name>§ePágina " + (page + 1) + "/" + totalPages
                        + " : desc>§7" + presets.size() + " skin(s) disponível(eis)."));

        this.setItem(SLOT_PROXIMA, BukkitUtils.deserializeItemStack(
                temProxima
                        ? "ARROW : 1 : name>§ePróxima página : desc>§7Clique para avançar uma página."
                        : "ARROW : 1 : name>§7Próxima página : desc>§7Você já está na última página."));
    }

    private ItemStack buildSkinItem(SkinPreset preset) {
        boolean emUso = isCurrentSkin(preset);

        String name = (emUso ? "§a✔ " : "§f") + preset.getDisplayName();
        String desc = emUso
                ? "§7Esta é a skin que você está\n§7utilizando atualmente.\n\n§a➜ §aClique para reaplicar."
                : "§7Clique para aplicar esta skin\n§7na sua conta.\n\n§a➜ §aClique aqui para aplicar.";

        return BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>" + name + " : desc>" + desc
                        + " : skin>" + preset.getSkinData().getValue());
    }

    private boolean isCurrentSkin(SkinPreset preset) {
        SkinData current = profile.getSkinData();
        return current != null && current.getValue() != null
                && current.getValue().equals(preset.getSkinData().getValue());
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (!evt.getInventory().equals(this.getInventory())) return;
        evt.setCancelled(true);

        if (!evt.getWhoClicked().equals(this.player)) return;
        if (evt.getClickedInventory() == null || !evt.getClickedInventory().equals(this.getInventory())) return;

        ItemStack item = evt.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = evt.getSlot();

        if (slot == SLOT_VOLTAR) {
            this.player.closeInventory();
            new MenuCustomize(profile);
            return;
        }

        if (slot == SLOT_ANTERIOR) {
            page--;
            renderPage();
            return;
        }

        if (slot == SLOT_PROXIMA) {
            page++;
            renderPage();
            return;
        }

        int gridIndex = indexOfGridSlot(slot);
        if (gridIndex == -1) return;

        int presetIndex = (page * PAGE_SIZE) + gridIndex;
        List<SkinPreset> presets = SkinLibrary.getAll();
        if (presetIndex < 0 || presetIndex >= presets.size()) return;

        SkinPreset preset = presets.get(presetIndex);
        applySkin(preset);
    }

    private int indexOfGridSlot(int slot) {
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            if (GRID_SLOTS[i] == slot) return i;
        }
        return -1;
    }


    private void applySkin(final SkinPreset preset) {
        Player target = profile.getPlayer();
        if (target == null || !target.isOnline()) {
            return;
        }

        SkinData skin = preset.getSkinData();

        VoidlessListeners.getSkinApplier().apply(target, skin);

        profile.setSkinData(skin);

        Main.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        profile.save(Main.getInstance().getStorage());
                    }
                });

        target.sendMessage("§aSkin §f" + preset.getDisplayName() + " §aaplicada e salva com sucesso!");

        renderPage();
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