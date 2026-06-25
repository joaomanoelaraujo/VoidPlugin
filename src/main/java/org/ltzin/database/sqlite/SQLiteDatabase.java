package org.ltzin.database.sqlite;

import org.ltzin.Main;
import org.ltzin.database.mysql.NonClosableConnection;
import org.ltzin.database.storage.StorageMetadata;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.logger.VLogger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements StorageImplementation {

    private volatile Connection connection;
    private final Object lock = new Object();

    private final Main main;
    private final VLogger logger;
    private final File file;

    public SQLiteDatabase(Main main, String fileName) {
        this.main = main;
        this.logger = new VLogger(main);
        this.file = new File(main.getDataFolder(), fileName + ".db");
    }

    @Override
    public Main getInstance() {
        return main;
    }

    @Override
    public String getImplementationName() {
        return "SQLite";
    }

    @Override
    public void init() throws Exception {
        synchronized (lock) {
            if (isConnectionAlive()) {
                logger.warning("Conexão SQLite já está ativa.");
                return;
            }

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            logger.info("Conectando ao banco de dados SQLite...");

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

            logger.info("Conectado ao SQLite com sucesso! (" + file.getName() + ")");
        }
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info("Conexão SQLite encerrada.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    @Override
    public StorageMetadata getMeta() {
        long size = file.exists() ? file.length() : 0L;
        return new StorageMetadata().connected(isConnectionAlive()).sizeBytes(size);
    }

    @Override
    public Connection getConnection() throws SQLException {
        synchronized (lock) {
            if (!isConnectionAlive()) {
                try {
                    init();
                } catch (Exception e) {
                    throw new SQLException("Falha ao reconectar ao SQLite: " + e.getMessage(), e);
                }
            }

            if (connection == null) {
                throw new SQLException("Conexão SQLite não pôde ser estabelecida.");
            }

            return new NonClosableConnection(connection);
        }
    }
    
    private boolean isConnectionAlive() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}