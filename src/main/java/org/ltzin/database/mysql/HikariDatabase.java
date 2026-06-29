package org.ltzin.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ltzin.Main;
import org.ltzin.database.storage.StorageMetadata;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.logger.VLogger;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariDatabase implements StorageImplementation {

    private HikariDataSource dataSource;
    private final VLogger logger;
    private final Main main;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int minIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;
    private final long keepaliveTime;

    public HikariDatabase(Main main, String host, int port, String database,
                          String username, String password,
                          int maxPoolSize, int minIdle,
                          long connectionTimeout, long idleTimeout, long maxLifetime,
                          long keepaliveTime) {
        this.main              = main;
        this.host              = host;
        this.port              = port;
        this.database          = database;
        this.username          = username;
        this.password          = password;
        this.maxPoolSize       = maxPoolSize;
        this.minIdle           = minIdle;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout       = idleTimeout;
        this.maxLifetime       = maxLifetime;
        this.keepaliveTime     = keepaliveTime;
        this.logger            = new VLogger(main);
    }

    @Override
    public void init() throws Exception {
        logger.info("Inicializando pool HikariCP...");

        HikariConfig config = new HikariConfig();


        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&characterEncoding=utf8&useUnicode=true"
                + "&serverTimezone=UTC");

        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("VoidlessPool");

        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);

        config.setKeepaliveTime(keepaliveTime);


        config.addDataSourceProperty("cachePrepStmts",           "true");
        config.addDataSourceProperty("prepStmtCacheSize",        "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit",    "2048");
        config.addDataSourceProperty("useServerPrepStmts",       "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata",   "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits",      "true");
        config.addDataSourceProperty("maintainTimeStats",        "false");
        config.addDataSourceProperty("useLocalSessionState",     "true");

        this.dataSource = new HikariDataSource(config);
        logger.info("Pool HikariCP iniciado com sucesso! (pool size: " + maxPoolSize + ")");
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool HikariCP encerrado.");
        }
    }

    @Override
    public String getImplementationName() { return "HikariCP"; }

    @Override
    public Main getInstance() { return main; }

    @Override
    public StorageMetadata getMeta() {
        boolean connected = dataSource != null && !dataSource.isClosed();
        return new StorageMetadata().connected(connected);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Pool HikariCP não está ativo.");
        }
        return dataSource.getConnection();
    }
}