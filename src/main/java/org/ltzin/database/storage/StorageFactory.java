package org.ltzin.database.storage;

import org.ltzin.Main;
import org.ltzin.database.mysql.MySQLDatabase;
import org.ltzin.database.mysql.HikariDatabase;
import org.ltzin.database.sqlite.SQLiteDatabase;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.database.type.StorageType;

public class StorageFactory {

    private final Main main;

    public StorageFactory(Main main) {
        this.main = main;
    }

    public StorageImplementation createNewImplementation(StorageType method) {
        switch (method) {
            case MYSQL: {
                return new MySQLDatabase(
                        main,
                        main.getConfig().getString("storage.mysql.host", "localhost"),
                        main.getConfig().getInt("storage.mysql.port", 3306),
                        main.getConfig().getString("storage.mysql.database", "minecraft"),
                        main.getConfig().getString("storage.mysql.username", "root"),
                        main.getConfig().getString("storage.mysql.password", "")
                );
            }
            case HIKARI: {
                return new HikariDatabase(
                        main,
                        main.getConfig().getString("storage.mysql.host", "localhost"),
                        main.getConfig().getInt("storage.mysql.port", 3306),
                        main.getConfig().getString("storage.mysql.database", "minecraft"),
                        main.getConfig().getString("storage.mysql.username", "root"),
                        main.getConfig().getString("storage.mysql.password", ""),
                        main.getConfig().getInt("storage.hikari.max-pool-size", 10),
                        main.getConfig().getInt("storage.hikari.min-idle", 2),
                        main.getConfig().getLong("storage.hikari.connection-timeout", 30000),
                        main.getConfig().getLong("storage.hikari.idle-timeout", 600000),
                        main.getConfig().getLong("storage.hikari.max-lifetime", 1800000),
                        main.getConfig().getLong("storage.hikari.keepalive-time", 60000)
                );
            }
            case SQLITE: {
                return new SQLiteDatabase(
                        main,
                        main.getConfig().getString("storage.sqlite.file", "database")
                );
            }
            default:
                throw new IllegalArgumentException("Tipo de storage desconhecido: " + method);
        }
    }
}