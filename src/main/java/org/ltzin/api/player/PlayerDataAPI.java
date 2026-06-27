package org.ltzin.api.player;

import org.ltzin.database.data.DataContainer;
import org.ltzin.database.data.DataTable;
import org.ltzin.database.data.interfaces.DataTableInfo;
import org.ltzin.database.storage.implementation.StorageImplementation;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerDataAPI {

    private static final Logger LOGGER = Logger.getLogger("VoidlessPlugin");

    private final StorageImplementation storage;

    public PlayerDataAPI(StorageImplementation storage) {
        this.storage = storage;
    }

    /**
     * Carrega os dados do jogador de uma tabela específica.
     * Retorna os defaults se o jogador não existir (e faz INSERT automático).
     *
     * Exemplo:
     *   Map<String, DataContainer> data = VoidlessAPI.get().players()
     *       .load("Steve", VoidlessTable.class);
     */
    public Map<String, DataContainer> load(String playerName, Class<? extends DataTable> tableClass) {
        DataTable table = resolveTable(tableClass);
        if (table == null) return null;

        DataTableInfo info = table.getInfo();

        try (Connection conn = storage.getConnection();
             PreparedStatement ps = conn.prepareStatement(info.select())) {

            ps.setString(1, playerName.toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return fromResultSet(rs, table.getDefaultValues());
            } else {
                Map<String, DataContainer> defaults = table.getDefaultValues();
                insert(playerName, defaults, info);
                return defaults;
            }

        } catch (SQLException ex) {
            LOGGER.warning("[PlayerDataAPI] Erro ao carregar dados de " + playerName + ": " + ex.getMessage());
            return table.getDefaultValues();
        }
    }

    /**
     * Salva (UPDATE) os dados do jogador.
     * Só salva colunas marcadas como updated=true.
     *
     * Exemplo:
     *   VoidlessAPI.get().players().save("Steve", data, VoidlessTable.class);
     */
    public void save(String playerName, Map<String, DataContainer> data, Class<? extends DataTable> tableClass) {
        DataTable table = resolveTable(tableClass);
        if (table == null) return;

        DataTableInfo info = table.getInfo();

        try (Connection conn = storage.getConnection();
             PreparedStatement ps = conn.prepareStatement(info.update())) {

            int i = 1;
            for (Map.Entry<String, DataContainer> entry : data.entrySet()) {
                ps.setObject(i++, entry.getValue().get());
            }
            ps.setString(i, playerName.toLowerCase());
            ps.executeUpdate();

            data.values().forEach(dc -> dc.setUpdated(false));

        } catch (SQLException ex) {
            LOGGER.warning("[PlayerDataAPI] Erro ao salvar dados de " + playerName + ": " + ex.getMessage());
        }
    }

    /**
     * Retorna um campo específico do jogador diretamente do banco.
     *
     * Exemplo:
     *   DataContainer cash = VoidlessAPI.get().players()
     *       .get("Steve", "cash", VoidlessTable.class);
     */
    public DataContainer get(String playerName, String field, Class<? extends DataTable> tableClass) {
        Map<String, DataContainer> data = load(playerName, tableClass);
        return data != null ? data.get(field) : null;
    }

    /**
     * Atualiza um campo específico do jogador direto no banco.
     *
     * Exemplo:
     *   VoidlessAPI.get().players().set("Steve", "cash", new DataContainer(500L), VoidlessTable.class);
     */
    public void set(String playerName, String field, DataContainer value, Class<? extends DataTable> tableClass) {
        DataTable table = resolveTable(tableClass);
        if (table == null) return;

        DataTableInfo info = table.getInfo();
        String sql = "UPDATE `" + info.name() + "` SET `" + field + "` = ? WHERE LOWER(`name`) = ?";

        try (Connection conn = storage.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, value.get());
            ps.setString(2, playerName.toLowerCase());
            ps.executeUpdate();

        } catch (SQLException ex) {
            LOGGER.warning("[PlayerDataAPI] Erro ao setar campo '" + field + "' de " + playerName + ": " + ex.getMessage());
        }
    }

    /** Verifica se o jogador existe na tabela */
    public boolean exists(String playerName, Class<? extends DataTable> tableClass) {
        DataTable table = resolveTable(tableClass);
        if (table == null) return false;

        try (Connection conn = storage.getConnection();
             PreparedStatement ps = conn.prepareStatement(table.getInfo().select())) {

            ps.setString(1, playerName.toLowerCase());
            return ps.executeQuery().next();

        } catch (SQLException ex) {
            LOGGER.warning("[PlayerDataAPI] Erro ao verificar existência de " + playerName);
            return false;
        }
    }

    private void insert(String playerName, Map<String, DataContainer> defaults, DataTableInfo info) {
        try (Connection conn = storage.getConnection();
             PreparedStatement ps = conn.prepareStatement(info.insert())) {

            ps.setString(1, playerName);
            int i = 2;
            for (DataContainer dc : defaults.values()) {
                ps.setObject(i++, dc.get());
            }
            ps.executeUpdate();

        } catch (SQLException ex) {
            LOGGER.warning("[PlayerDataAPI] Erro ao inserir jogador: " + ex.getMessage());
        }
    }

    private Map<String, DataContainer> fromResultSet(ResultSet rs, Map<String, DataContainer> defaults) throws SQLException {
        Map<String, DataContainer> data = new LinkedHashMap<>();
        for (String key : defaults.keySet()) {
            Object val = rs.getObject(key);
            data.put(key, new DataContainer(val != null ? val : defaults.get(key).get()));
        }
        return data;
    }

    private DataTable resolveTable(Class<? extends DataTable> tableClass) {
        return DataTable.listTables().stream()
                .filter(t -> t.getClass().equals(tableClass))
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.warning("[PlayerDataAPI] Tabela não registrada: " + tableClass.getSimpleName());
                    return null;
                });
    }
}