package org.ltzin.libraries;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PagedMenu {

    private int previousSlot = 45;
    private int nextSlot     = 53;

    private ItemStack previousItem = null;
    private ItemStack nextItem     = null;

    protected int rows;
    protected String name;

    protected final List<Menu>         pages  = new ArrayList<>();
    protected final Map<Menu, Integer> pageId = new HashMap<>();

    protected final Map<Integer, ItemStack> blockedSlots = new HashMap<>();

    private int lastListSize = -1;

    public PagedMenu(String name) {
        this(name, 3);
    }

    public PagedMenu(String name, int rows) {
        this.name = name;
        this.rows = Math.min(Math.max(1, rows), 6);
    }

    public void open(Player player) {
        if (pages.isEmpty()) return;
        player.openInventory(pages.get(0).getInventory());
    }

    public void openPrevious(Player player, Inventory currentInventory) {
        int page = getPageOf(currentInventory);
        if (page <= 1) return;
        player.openInventory(pages.get(page - 2).getInventory());
    }

    public void openNext(Player player, Inventory currentInventory) {
        int page = getPageOf(currentInventory);
        if (page == -1 || page >= pages.size()) return;
        player.openInventory(pages.get(page).getInventory());
    }

    public int getPageOf(Inventory inventory) {
        for (Menu menu : pages) {
            if (menu.getInventory().equals(inventory)) {
                return pageId.get(menu);
            }
        }
        return -1;
    }

    public void onlySlots(Integer... slots) {
        onlySlots(Arrays.asList(slots));
    }

    public void onlySlots(List<Integer> allowed) {
        for (int slot = 0; slot < rows * 9; slot++) {
            if (!allowed.contains(slot)) {
                blockedSlots.put(slot, null);
            }
        }
    }

    public void removeSlots(int... slots) {
        for (int slot : slots) blockedSlots.put(slot, null);
    }

    public void removeSlotsWith(ItemStack item, int... slots) {
        for (int slot : slots) blockedSlots.put(slot, item);
    }


    public void setItems(List<ItemStack> items) {
        if (items.size() == lastListSize && !pages.isEmpty()) {
            updateItems(items);
            return;
        }

        pages.forEach(menu -> new ArrayList<>(menu.getInventory().getViewers())
                .forEach(HumanEntity::closeInventory));
        pages.clear();
        pageId.clear();
        lastListSize = items.size();

        List<List<ItemStack>> split = splitItems(items);
        if (split.isEmpty()) split.add(new ArrayList<>());

        for (int i = 0; i < split.size(); i++) {
            Menu menu = new Menu(name, rows);

            blockedSlots.forEach((slot, decoration) -> {
                menu.getSlots().remove(slot);
                if (decoration != null) menu.setItem(slot, decoration);
            });

            menu.setItems(split.get(i));

            if (split.size() > 1) {
                if (i > 0 && previousSlot != -1) {
                    menu.setItem(previousSlot, buildNavItem(previousItem, "§7← Página " + i));
                }
                if (i + 1 < split.size() && nextSlot != -1) {
                    menu.setItem(nextSlot, buildNavItem(nextItem, "§7Página " + (i + 2) + " →"));
                }
            }

            pages.add(menu);
            pageId.put(menu, i + 1);
        }
    }

    public void updateItems(List<ItemStack> items) {
        List<List<ItemStack>> split = splitItems(items);
        if (split.isEmpty()) split.add(new ArrayList<>());

        for (int i = 0; i < split.size() && i < pages.size(); i++) {
            Menu menu = pages.get(i);
            blockedSlots.forEach((slot, decoration) -> {
                if (decoration != null) menu.setItem(slot, decoration);
            });
            menu.setItems(split.get(i));
        }
    }

    public List<Menu> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public int getPageCount() {
        return pages.size();
    }

    public int getPreviousSlot() { return previousSlot; }
    public int getNextSlot()     { return nextSlot; }

    public void setPreviousSlot(int slot) { this.previousSlot = slot; }
    public void setNextSlot(int slot)     { this.nextSlot = slot; }

    public void setPreviousItem(ItemStack item) { this.previousItem = item; }

    public void setNextItem(ItemStack item) { this.nextItem = item; }

    protected int pageCapacity() {
        return (rows * 9) - blockedSlots.size();
    }

    private List<List<ItemStack>> splitItems(List<ItemStack> items) {
        List<List<ItemStack>> result = new ArrayList<>();
        int capacity = pageCapacity();
        if (capacity <= 0) capacity = 1;

        List<ItemStack> current = new ArrayList<>();
        for (ItemStack item : items) {
            current.add(item);
            if (current.size() == capacity) {
                result.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) result.add(current);
        return result;
    }

    private ItemStack buildNavItem(ItemStack custom, String defaultName) {
        if (custom != null) return custom;
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(defaultName);
            arrow.setItemMeta(meta);
        }
        return arrow;
    }
}