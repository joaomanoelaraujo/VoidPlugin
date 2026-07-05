package org.ltzin.hotbar;

public class HotbarButton {

  private final int slot;
  private final HotbarAction action;
  private final String icon; // formato aceito por BukkitUtils.deserializeItemStack

  /**
   * @param slot slot no formato 1-9 (humano), convertido internamente pra 0-8.
   * @param icon string no formato "MATERIAL : quantidade : name>... : desc>... : owner>...",
   *             o mesmo formato que BukkitUtils.serializeItemStack gera.
   *             Use "owner>%player_head%" pra colocar a skin real de quem recebeu a hotbar.
   */
  public HotbarButton(int slot, HotbarAction action, String icon) {
    this.slot = slot - 1;
    this.action = action;
    this.icon = icon;
  }

  public int getSlot() {
    return this.slot;
  }

  public HotbarAction getAction() {
    return this.action;
  }

  public String getIcon() {
    return this.icon;
  }
}