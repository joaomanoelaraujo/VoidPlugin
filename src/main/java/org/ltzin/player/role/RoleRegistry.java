package org.ltzin.player.role;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ltzin.Main;

import java.io.File;
import java.util.logging.Logger;

public final class RoleRegistry {

    private RoleRegistry() {}

    private static final Logger LOG = Logger.getLogger("RoleRegistry");

    public static void setup() {
        Role.clear();

        File file = new File(Main.getInstance().getDataFolder(), "ranks.yml");

        if (!file.exists()) {
            Main.getInstance().saveResource("ranks.yml", false);
            LOG.info("[RoleRegistry] ranks.yml criado com os valores padrão.");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.isConfigurationSection("ranks")) {
            LOG.severe("[RoleRegistry] Nenhum rank encontrado em ranks.yml! O servidor não funcionará corretamente.");
            return;
        }

        int count = 0;
        for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
            String path         = "ranks." + key + ".";
            String prefix       = config.getString(path + "prefix",       "&7");
            String tagDisplay   = config.getString(path + "tag",          "");
            String permission   = config.getString(path + "permission",   "");
            boolean alwaysVisible = config.getBoolean(path + "alwaysvisible", false);
            boolean broadcast   = config.getBoolean(path + "broadcast",   false);
            boolean fly         = config.getBoolean(path + "fly",         false);

            String name = key.substring(0, 1).toUpperCase() + key.substring(1);

            Role.register(new Role(name, prefix, tagDisplay, permission, alwaysVisible, broadcast, fly));
            count++;
        }

        if (count == 0) {
            LOG.severe("[RoleRegistry] ranks.yml não contém nenhum rank válido!");
            return;
        }

        Role last = Role.getDefault();
        if (!last.isDefault()) {
            LOG.warning("[RoleRegistry] O último rank '" + last.getName()
                    + "' tem permissão definida. O último rank deve ser o padrão (permission vazia).");
        }

        LOG.info("[RoleRegistry] " + count + " rank(s) carregado(s) de ranks.yml. Padrão: " + last.getName());
    }

    public static void reload() {
        setup();
    }
}