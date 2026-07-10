package org.ltzin.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HologramManager {

    private final JavaPlugin plugin;
    private final Map<String, Hologram> holograms = new LinkedHashMap<>();
    private final File file;

    public HologramManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "hologramas.yml");
    }

    public Hologram create(String id, Location location) {
        Hologram hologram = new Hologram(id, location);
        holograms.put(id.toLowerCase(), hologram);
        return hologram;
    }

    public Hologram get(String id) {
        return holograms.get(id.toLowerCase());
    }

    public boolean exists(String id) {
        return holograms.containsKey(id.toLowerCase());
    }

    public void remove(String id) {
        Hologram h = holograms.remove(id.toLowerCase());
        if (h != null) h.remove();
    }

    public Map<String, Hologram> getAll() {
        return holograms;
    }

    /** Remove todas as entidades do mundo (chamar em onDisable). */
    public void removeAllEntities() {
        for (Hologram h : holograms.values()) {
            h.remove();
        }
    }

    /** Recria todas as entidades (chamar em onEnable, depois de load()). */
    public void spawnAll() {
        for (Hologram h : holograms.values()) {
            h.spawnAll();
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Hologram h : holograms.values()) {
            String path = "hologramas." + h.getId();
            Location loc = h.getBaseLocation();
            config.set(path + ".mundo", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            java.util.List<String> texts = new java.util.ArrayList<>();
            for (HologramLine line : h.getLines()) texts.add(line.getText());
            config.set(path + ".linhas", texts);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar hologramas.yml: " + e.getMessage());
        }
    }

    public void load() {
        holograms.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("hologramas");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String path = "hologramas." + id;
            String worldName = config.getString(path + ".mundo");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue;

            Location loc = new Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z")
            );

            Hologram hologram = create(id, loc);
            for (String linha : config.getStringList(path + ".linhas")) {
                hologram.addLine(linha);
            }
        }
    }
}
