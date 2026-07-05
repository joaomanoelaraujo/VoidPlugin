package org.ltzin.hotbar;

import org.bukkit.inventory.ItemStack;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;

import java.util.ArrayList;
import java.util.List;

public class Hotbar {

  private static final List<Hotbar> HOTBARS = new ArrayList<>();

  private final String id;
  private final List<HotbarButton> buttons;

  public Hotbar(String id) {
    this.id = id;
    this.buttons = new ArrayList<>();
    HOTBARS.add(this);
  }

  public static Hotbar getHotbarById(String id) {
    return HOTBARS.stream().filter(hb -> hb.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
  }

  public String getId() {
    return this.id;
  }

  public static void addHotbar(Hotbar hotbar) {
    HOTBARS.add(hotbar);
  }


  public List<HotbarButton> getButtons() {
    return this.buttons;
  }

  public Hotbar addButton(HotbarButton button) {
    this.buttons.add(button);
    return this;
  }

  public void apply(Profile profile) {
    profile.getPlayer().getInventory().clear();
    profile.getPlayer().getInventory().setArmorContents(null);

    for (HotbarButton button : this.buttons) {
      if (button.getSlot() < 0 || button.getSlot() > 8) {
        continue;
      }

      String template = button.getIcon();
      boolean ownSkin = template.contains("%player_head%");

      String raw = template.replace("%player_head%", "").replace("%player%", profile.getPlayer().getName());
      ItemStack icon = BukkitUtils.deserializeItemStack(raw);

      if (ownSkin) {
        icon = BukkitUtils.putProfileOnSkull(profile.getPlayer(), icon);
      }

      profile.getPlayer().getInventory().setItem(button.getSlot(), icon);
    }

    profile.getPlayer().updateInventory();
  }

  public HotbarButton getButtonBySlot(int slot) {
    return this.buttons.stream().filter(button -> button.getSlot() == slot).findFirst().orElse(null);
  }

  public HotbarButton compareButton(Profile profile, ItemStack item) {
    if (item == null) {
      return null;
    }
    return this.buttons.stream()
        .filter(button -> button.getSlot() >= 0 && button.getSlot() <= 8)
        .filter(button -> item.equals(profile.getPlayer().getInventory().getItem(button.getSlot())))
        .findFirst()
        .orElse(null);
  }
}