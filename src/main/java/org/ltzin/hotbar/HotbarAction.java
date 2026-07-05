package org.ltzin.hotbar;

import org.bukkit.entity.Player;
import org.ltzin.player.Profile;

/**
 * Representa uma acao no formato "tipo>valor", ex: "comando>tp menu".
 * Usa split limitado a 2 partes pra nao quebrar valores que contenham ">".
 */
public class HotbarAction {

  private final String value;
  private final HotbarActionType actionType;

  public HotbarAction(String action) {
    String[] splitter = action.split(">", 2);
    this.actionType = HotbarActionType.fromName(splitter[0]);
    this.value = splitter.length > 1 ? splitter[1] : "";
  }

  public void execute(Profile profile) {
    if (this.actionType == null || this.value.isEmpty()) {
      return;
    }
    this.actionType.execute(profile, this.value);
  }

  public String getValue() {
    return this.value;
  }

  public HotbarActionType getActionType() {
    return this.actionType;
  }
}