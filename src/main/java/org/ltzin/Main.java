package org.ltzin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.ltzin.api.VoidlessAPI;
import org.ltzin.api.database.StorageAPI;
import org.ltzin.api.player.PlayerDataAPI;
import org.ltzin.api.table.TableAPI;
import org.ltzin.commands.Commands;
import org.ltzin.database.DatabaseManager;
import org.ltzin.database.storage.StorageFactory;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.database.type.StorageType;
import org.ltzin.deliveries.Delivery;
import org.ltzin.hologram.HologramManager;
import org.ltzin.listeners.PreferencesListener;
import org.ltzin.listeners.VoidlessListeners;
import org.ltzin.logger.VLogger;
import org.ltzin.nms.NMS;
import org.ltzin.npc.NPCManager;
import org.ltzin.player.role.RoleRegistry;
import org.ltzin.plugin.hook.VoidlessExpansion;
import org.ltzin.tab.TabManager;

import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {

    private static Main plugin;
    private VLogger logger;
    private StorageImplementation storage;
    private DatabaseManager databaseManager;
    private VoidlessAPI api;
    private HologramManager hologramManager;
    private NPCManager npcManager;
    private static Location lobby;
    public static final List<String> minigames = Arrays.asList("Sky Wars");
    public static String minigame = "";


    @Override
    public void onEnable() {
        plugin = this;
        logger = new VLogger(this);

        saveDefaultConfig();
        hologramManager = new HologramManager(this);
        npcManager = new NPCManager(this);

        RoleRegistry.setup();

        String rawType = getConfig().getString("storage.type", "SQLITE");
        StorageType storageType = StorageType.parse(rawType, StorageType.SQLITE);

        logger.info("Tipo de storage selecionado: " + storageType.getName());

        storage = new StorageFactory(this).createNewImplementation(storageType);

        try {
            storage.init();
        } catch (Exception e) {
            logger.warning("Falha ao inicializar o storage (" + storageType.getName() + "): " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(storage, logger);
        databaseManager.setupTables();

        api = new VoidlessAPI(
                new StorageAPI(storage),
                new TableAPI(databaseManager),
                new PlayerDataAPI(storage)
        );

        hologramManager.load();
        hologramManager.spawnAll();
        npcManager.load();
        npcManager.spawnAllToEveryone();

        Delivery.setupDeliveries();
        TabManager.setup();
        VoidlessAPI.init(api);
        VoidlessListeners.setup();
        Commands.setupCommands();
        PreferencesListener.setup();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("PlaceholderAPI nao encontrado. Os placeholders \"%voidless_*%\" nao estarao disponiveis.");
            return;
        }

        new VoidlessExpansion().register();

        logger.info("Plugin iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.shutdown();
        }
        if (hologramManager != null) {
            hologramManager.removeAllEntities();
        }
        if (npcManager != null) {
            npcManager.removeAll();
        }

        logger.info("Plugin finalizado com sucesso!");
        plugin = null;
    }

    public static void setLobby(Location location) {
        lobby = location;
    }

    public static Location getLobby() {
        return lobby;
    }
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    public NPCManager getNpcManager() {
        return npcManager;
    }
    public StorageImplementation getStorage()      { return storage; }
    public DatabaseManager getDatabaseManager()    { return databaseManager; }
    public VoidlessAPI getAPI()                    { return api; }
    public VLogger getMyLogger()                   { return logger; }
    public static Main getInstance()               { return plugin; }
}