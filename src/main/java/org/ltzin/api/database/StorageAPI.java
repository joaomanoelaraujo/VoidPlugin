package org.ltzin.api.database;

import org.ltzin.database.storage.StorageMetadata;
import org.ltzin.database.storage.implementation.StorageImplementation;

import java.sql.Connection;
import java.sql.SQLException;

public class StorageAPI {

    private final StorageImplementation storage;

    public StorageAPI(StorageImplementation storage) {
        this.storage = storage;
    }

    public Connection getConnection() throws SQLException {
        return storage.getConnection();
    }

    public String getImplementationName() {
        return storage.getImplementationName();
    }

    public boolean isSQLite() {
        return storage.getImplementationName().equalsIgnoreCase("SQLite");
    }

    public boolean isMySQL() {
        String name = storage.getImplementationName();
        return name.equalsIgnoreCase("MySQL") || name.equalsIgnoreCase("MariaDB");
    }

    public StorageMetadata getMeta() {
        return storage.getMeta();
    }

    public StorageImplementation getRaw() {
        return storage;
    }
}