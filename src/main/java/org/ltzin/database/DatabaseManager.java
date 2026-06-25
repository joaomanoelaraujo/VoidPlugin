package org.ltzin.database;

import org.ltzin.database.data.DataTable;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.logger.VLogger;

public class DatabaseManager {

    private final StorageImplementation storage;
    private final VLogger logger;

    public DatabaseManager(StorageImplementation storage, VLogger logger) {
        this.storage = storage;
        this.logger  = logger;
    }

    public void setupTables() {
        for (DataTable table : DataTable.listTables()) {
            String name = table.getInfo().name();
            try {
                table.setup(storage);
                logger.info("Tabela '" + name + "' inicializada.");
            } catch (Exception ex) {
                logger.warning("Falha ao inicializar tabela '" + name + "': " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public StorageImplementation getStorage() {
        return storage;
    }
}