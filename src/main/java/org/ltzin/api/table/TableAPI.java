package org.ltzin.api.table;

import org.ltzin.database.DatabaseManager;
import org.ltzin.database.data.DataTable;
import org.ltzin.database.data.interfaces.DataTableInfo;

import java.util.Collection;

public class TableAPI {

    private final DatabaseManager databaseManager;

    public TableAPI(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    public void register(DataTable table) {
        DataTable.registerTable(table);
        table.setup(databaseManager.getStorage());
    }

    public boolean isRegistered(String tableName) {
        return DataTable.listTables().stream()
                .anyMatch(t -> t.getInfo().name().equals(tableName));
    }

    public Collection<DataTable> listAll() {
        return DataTable.listTables();
    }

    public DataTableInfo getInfo(String tableName) {
        return DataTable.listTables().stream()
                .filter(t -> t.getInfo().name().equals(tableName))
                .map(DataTable::getInfo)
                .findFirst()
                .orElse(null);
    }
}