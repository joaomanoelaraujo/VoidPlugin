package org.ltzin.hotbar;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Carrega hotbars a partir de uma secao do config.yml.
 *
 * Exemplo de config.yml (icon usa o mesmo formato de BukkitUtils.serializeItemStack):
 *
 * hotbars:
 *   lobby:
 *     1:
 *       icon: 'COMPASS : 1 : name>&aTeletransporte : desc>&7Clique para abrir o menu'
 *       action: 'comando>tp menu'
 *     5:
 *       icon: 'PLAYER_HEAD : 1 : name>&aPerfil : owner>%player_head%'
 *       action: 'comando>perfil'
 *     9:
 *       icon: 'REDSTONE : 1 : name>&cSair'
 *       action: 'comando>lobby'
 *
 * Uso no onEnable:
 * HotbarManager.loadFromConfig(getConfig().getConfigurationSection("hotbars"));
 * getServer().getPluginManager().registerEvents(new HotbarListener(), this);
 */
public class HotbarManager {

  public static void loadFromConfig(ConfigurationSection section) {
    if (section == null) {
      return;
    }

    for (String hotbarId : section.getKeys(false)) {
      ConfigurationSection hotbarSection = section.getConfigurationSection(hotbarId);
      Hotbar hotbar = new Hotbar(hotbarId);

      for (String slotKey : hotbarSection.getKeys(false)) {
        ConfigurationSection buttonSection = hotbarSection.getConfigurationSection(slotKey);
        int slot = Integer.parseInt(slotKey);

        String icon = buttonSection.getString("icon", "STONE : 1");
        String actionRaw = buttonSection.getString("action", "");

        hotbar.addButton(new HotbarButton(slot, new HotbarAction(actionRaw), icon));
      }
    }
  }
}