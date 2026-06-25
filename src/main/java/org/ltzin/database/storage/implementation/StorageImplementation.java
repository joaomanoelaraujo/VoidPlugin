package org.ltzin.database.storage.implementation;

import org.ltzin.Main;
import org.ltzin.database.storage.StorageMetadata;

import java.sql.Connection;
import java.sql.SQLException;

public interface StorageImplementation {

    Main getInstance();

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    StorageMetadata getMeta();

    Connection getConnection() throws SQLException;
}