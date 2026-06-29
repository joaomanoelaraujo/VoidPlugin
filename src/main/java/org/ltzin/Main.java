package org.ltzin;

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
import org.ltzin.listeners.PreferencesListener;
import org.ltzin.listeners.VoidlessListeners;
import org.ltzin.logger.VLogger;
import org.ltzin.player.role.RoleRegistry;
import org.ltzin.tab.TabManager;

public class Main extends JavaPlugin {

    private static Main plugin;
    private VLogger logger;
    private StorageImplementation storage;
    private DatabaseManager databaseManager;
    private VoidlessAPI api;

    @Override
    public void onEnable() {
        plugin = this;
        logger = new VLogger(this);

        saveDefaultConfig();

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

        Delivery.setupDeliveries();
        TabManager.setup();
        VoidlessAPI.init(api);
        VoidlessListeners.setup();
        Commands.setupCommands();
        PreferencesListener.setup();
        logger.info("Plugin iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.shutdown();
        }
        logger.info("Plugin finalizado com sucesso!");
        plugin = null;
    }

    public StorageImplementation getStorage()      { return storage; }
    public DatabaseManager getDatabaseManager()    { return databaseManager; }
    public VoidlessAPI getAPI()                    { return api; }
    public VLogger getMyLogger()                   { return logger; }
    public static Main getInstance()               { return plugin; }
}