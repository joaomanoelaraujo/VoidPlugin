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
import org.ltzin.deliveries.Delivery;
import org.ltzin.deliveries.DeliveriesContainer;
import org.ltzin.libraries.PlayerMenu;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;
import org.ltzin.utils.EnumSound;

import java.util.ArrayList;
import java.util.List;

public class MenuDeliveries extends PlayerMenu {

    private static final int[] DELIVERY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int ITEMS_PER_PAGE = DELIVERY_SLOTS.length; // 28

    private static final int SLOT_PREV = 45;
    private static final int SLOT_NEXT = 53;


    private static final String BTN_PREV =
            "SKULL_ITEM:3 : 1 : name>&ePágina Anterior"
            + " : desc>&7Clique para voltar uma página."
            + " : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA3M2VhODZhYTUzMWFhZjQyMGQzNzZkMzdmZmI2ZGIzNzM3YiJ9fX0=";

    private static final String BTN_NEXT =
            "SKULL_ITEM:3 : 1 : name>&ePágina Seguinte"
            + " : desc>&7Clique para avançar uma página."
            + " : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWQ1NGViYTVlZmYyMDgxMzExMmUyNmJlZmM5OThlNTQyNTQ3OTFhYWMzZDQ3ZWMifX19";


    private final Profile profile;
    private int page;
    private final List<Delivery> deliveries;

    public MenuDeliveries(Profile profile) {
        this(profile, 0);
    }

    public MenuDeliveries(Profile profile, int page) {
        super(profile.getPlayer(), "Entregas", 6);
        this.profile    = profile;
        this.page       = page;
        this.deliveries = new ArrayList<>(Delivery.listDeliveries());



        build();
        this.register(Main.getInstance());
        this.open();
    }

    private void build() {
        this.getInventory().clear();


        int start = page * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, deliveries.size());

        for (int i = start; i < end; i++) {
            this.setItem(DELIVERY_SLOTS[i - start], deliveries.get(i).getIcon(profile));
        }

        if (page > 0) {
            this.setItem(SLOT_PREV, BukkitUtils.deserializeItemStack(BTN_PREV));
        }
        if (page < getTotalPages() - 1) {
            this.setItem(SLOT_NEXT, BukkitUtils.deserializeItemStack(BTN_NEXT));
        }

        this.setItem(49, BukkitUtils.deserializeItemStack("ARROW : 1 : name>§cVoltar : desc>§7Para o perfil."));
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) deliveries.size() / ITEMS_PER_PAGE));
    }

    private boolean isDeliverySlot(int slot) {
        for (int ds : DELIVERY_SLOTS) {
            if (ds == slot) return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (!evt.getInventory().equals(this.getInventory())) return;
        evt.setCancelled(true);

        if (!evt.getWhoClicked().equals(this.player)) return;
        if (evt.getClickedInventory() == null) return;
        if (!evt.getClickedInventory().equals(this.getInventory())) return;

        Profile current = Profile.getProfile(this.player.getName());
        if (current == null) {
            this.player.closeInventory();
            return;
        }

        ItemStack item = evt.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = evt.getSlot();

        if (slot == SLOT_PREV && page > 0) {
            EnumSound.CLICK.play(this.player, 1.0F, 1.0F);
            page--;
            build();
            return;
        }

        if (slot == SLOT_NEXT && page < getTotalPages() - 1) {
            EnumSound.CLICK.play(this.player, 1.0F, 1.0F);
            page++;
            build();
            return;
        }
        if (slot == 49) {
            EnumSound.CLICK.play(this.player, 1.0F, 1.0F);
            new MenuProfile(profile);
            return;
        }

        int relativeIdx = relativeDeliveryIndex(slot);
        if (relativeIdx < 0) return;

        int absoluteIdx = page * ITEMS_PER_PAGE + relativeIdx;
        if (absoluteIdx >= deliveries.size()) return;

        handleDeliveryClick(current, deliveries.get(absoluteIdx));
    }

    private void handleDeliveryClick(Profile profile, Delivery delivery) {
        Delivery.ClaimState state = delivery.getState(profile);

        switch (state) {
            case NO_PERMISSION:
                EnumSound.VILLAGER_NO.play(this.player, 1.0F, 1.0F);
                this.player.sendMessage("§cVocê não tem permissão para coletar esta entrega!");
                break;

            case ON_COOLDOWN:
                EnumSound.VILLAGER_NO.play(this.player, 1.0F, 1.0F);
                DeliveriesContainer container =
                        profile.getContainer("VoidProfile", "delivery_claims", DeliveriesContainer.class);
                long remaining = container.getTimeUntilNextClaim(delivery.getId(), delivery.getCooldownMillis());
                this.player.sendMessage("§cEsta entrega estará disponível em §f"
                        + formatTime(remaining) + "§c.");
                break;

            case AVAILABLE:
                if (delivery.claim(profile)) {
                    EnumSound.LEVEL_UP.play(this.player, 1.0F, 1.0F);
                    if (!delivery.getMessage().isEmpty()) {
                        this.player.sendMessage(delivery.getMessage()
                                .replace("{player}", this.player.getName()));
                    }
                    build();
                } else {
                    EnumSound.VILLAGER_NO.play(this.player, 1.0F, 1.0F);
                    this.player.sendMessage("§cOcorreu um erro ao coletar a entrega.");
                }
                break;
        }
    }

    /** Mapeamento de slot do inventário → índice dentro de DELIVERY_SLOTS. */
    private int relativeDeliveryIndex(int slot) {
        for (int i = 0; i < DELIVERY_SLOTS.length; i++) {
            if (DELIVERY_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "0s";
        long d  = millis / 86400000L;
        long h  = (millis % 86400000L) / 3600000L;
        long m  = (millis % 3600000L)  / 60000L;
        long s  = (millis % 60000L)    / 1000L;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
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
        if (evt.getPlayer().equals(this.player)
                && evt.getInventory().equals(this.getInventory())) {
            this.cancel();
        }
    }
}