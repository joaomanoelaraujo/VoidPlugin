package org.ltzin.database.data;

import org.ltzin.database.data.interfaces.DataTableInfo;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.database.tables.SkyWarsTable;
import org.ltzin.database.tables.VoidlessTable;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DataTable {

    public static final Logger LOGGER = Logger.getLogger("VoidlessPlugin");

    private static final List<DataTable> TABLES = new ArrayList<>();

    static {
        TABLES.add(new SkyWarsTable());
        TABLES.add(new VoidlessTable());
    }

    public static void registerTable(DataTable table) {
        TABLES.add(table);
    }

    public static Collection<DataTable> listTables() {
        return Collections.unmodifiableList(TABLES);
    }


    public final void setup(StorageImplementation storage) {
        boolean isSQLite = storage.getImplementationName().equalsIgnoreCase("SQLite");
        DataTableInfo info = getInfo();

        try (Connection conn = storage.getConnection();
             Statement stmt = conn.createStatement()) {

            String createSQL = isSQLite
                    ? toSQLiteCreate(info.create())
                    : info.create();

            stmt.execute(createSQL);

        } catch (SQLException ex) {
            LOGGER.warning("[DataTable] Erro ao criar tabela '" + info.name() + "': " + ex.getMessage());
            return;
        }

        if (!isSQLite) {
            autoMigrateColumns(storage, info);
        } else {
            autoMigrateColumnsSQLite(storage, info);
        }

        init(storage);
    }

    public void init(StorageImplementation storage) {}


    private void autoMigrateColumns(StorageImplementation storage, DataTableInfo info) {
        Map<String, ColDef> expected = parseCreateStatement(info.create());
        Map<String, ColDef> current  = getCurrentColumnsMySQL(storage, info.name());

        for (Map.Entry<String, ColDef> entry : expected.entrySet()) {
            String name = entry.getKey();
            ColDef exp  = entry.getValue();

            if (!current.containsKey(name)) {
                execute(storage, "ALTER TABLE `" + info.name() + "` ADD COLUMN `" + name + "` " + exp.definition);
                LOGGER.info("[DataTable] Coluna adicionada: " + name + " em " + info.name());
            } else {
                ColDef cur = current.get(name);
                if (!cur.type.equalsIgnoreCase(exp.type)) {
                    execute(storage, "ALTER TABLE `" + info.name() + "` MODIFY COLUMN `" + name + "` " + exp.definition);
                    LOGGER.info("[DataTable] Coluna modificada: " + name + " em " + info.name());
                }
            }
        }
    }

    private Map<String, ColDef> getCurrentColumnsMySQL(StorageImplementation storage, String tableName) {
        Map<String, ColDef> cols = new LinkedHashMap<>();
        try (Connection conn = storage.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM `" + tableName + "`")) {
            while (rs.next()) {
                String colName = rs.getString("Field");
                String type    = rs.getString("Type");
                cols.put(colName, new ColDef(type, type));
            }
        } catch (SQLException ex) {
            LOGGER.warning("[DataTable] Erro ao ler colunas de " + tableName + ": " + ex.getMessage());
        }
        return cols;
    }

    private void autoMigrateColumnsSQLite(StorageImplementation storage, DataTableInfo info) {
        Set<String> currentCols = new HashSet<>();
        try (Connection conn = storage.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("PRAGMA table_info(" + info.name() + ")")) {
            while (rs.next()) {
                currentCols.add(rs.getString("name").toLowerCase());
            }
        } catch (SQLException ex) {
            LOGGER.warning("[DataTable] Erro PRAGMA em " + info.name() + ": " + ex.getMessage());
            return;
        }

        Map<String, ColDef> expected = parseCreateStatement(info.create());
        for (Map.Entry<String, ColDef> entry : expected.entrySet()) {
            if (!currentCols.contains(entry.getKey().toLowerCase())) {
                execute(storage, "ALTER TABLE `" + info.name() + "` ADD COLUMN `" + entry.getKey() + "` " + entry.getValue().definition);
                LOGGER.info("[DataTable] SQLite coluna adicionada: " + entry.getKey());
            }
        }
    }


    private void execute(StorageImplementation storage, String sql) {
        try (Connection conn = storage.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ex) {
            LOGGER.warning("[DataTable] Erro ao executar SQL: " + ex.getMessage());
        }
    }

    private String toSQLiteCreate(String mysqlCreate) {
        return mysqlCreate
                .replaceAll("ENGINE=InnoDB[^;]*", "")
                .replaceAll("DEFAULT CHARSET=\\w+", "")
                .replaceAll("COLLATE\\s+\\w+", "")
                .replaceAll("(?i)\\bLONG\\b", "INTEGER")
                .replaceAll("(?i)VARCHAR\\(\\d+\\)", "TEXT")
                .trim();
    }

    private Map<String, ColDef> parseCreateStatement(String createSQL) {
        Map<String, ColDef> cols = new LinkedHashMap<>();
        Pattern p = Pattern.compile("`(\\w+)`\\s+([A-Za-z]+[^,)]*?)(?:,|\\s*\\)|\\s+PRIMARY|\\s+ENGINE)");
        Matcher m = p.matcher(createSQL);
        while (m.find()) {
            String colName = m.group(1);
            String def     = m.group(2).trim();
            if (colName.equalsIgnoreCase("PRIMARY") || colName.equalsIgnoreCase("INDEX")) continue;
            cols.put(colName, new ColDef(def.split("\\s+")[0], def));
        }
        return cols;
    }

    private static class ColDef {
        String type, definition;
        ColDef(String type, String definition) { this.type = type; this.definition = definition; }
    }


    public abstract Map<String, DataContainer> getDefaultValues();

    public DataTableInfo getInfo() {
        return this.getClass().getAnnotation(DataTableInfo.class);
    }
}