package org.ltzin.database.mysql;

import org.ltzin.Main;
import org.ltzin.database.storage.StorageMetadata;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.logger.VLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase implements StorageImplementation {

    private volatile Connection connection;
    private final Object lock = new Object();

    private final VLogger logger;
    private final Main main;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLDatabase(Main main, String host, int port, String database, String username, String password) {
        this.main = main;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.logger = new VLogger(main);
    }

    @Override
    public Main getInstance() {
        return main;
    }

    @Override
    public String getImplementationName() {
        return "MySQL";
    }

    @Override
    public void init() throws Exception {
        synchronized (lock) {
            if (isConnectionAlive()) {
                logger.warning("Conexão MySQL já está ativa.");
                return;
            }

            logger.info("Conectando ao banco de dados MySQL...");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&autoReconnect=true&allowPublicKeyRetrieval=true";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver não encontrado!", e);
            }

            this.connection = DriverManager.getConnection(url, username, password);
            logger.info("Conectado ao MySQL com sucesso!");
        }
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info("Conexão MySQL encerrada.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Invalida a referência para que getConnection() saiba que precisa reconectar
                connection = null;
            }
        }
    }

    @Override
    public StorageMetadata getMeta() {
        return new StorageMetadata().connected(isConnectionAlive());
    }

    @Override
    public Connection getConnection() throws SQLException {
        synchronized (lock) {
            if (!isConnectionAlive()) {
                try {
                    init();
                } catch (Exception e) {
                    throw new SQLException("Falha ao reconectar ao MySQL: " + e.getMessage(), e);
                }
            }
            return new NonClosableConnection(connection);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Verifica se a conexão subjacente está viva sem lançar exceção não tratada.
     */
    private boolean isConnectionAlive() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}