package org.ltzin.player;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.ltzin.Main;
import org.ltzin.database.data.DataContainer;
import org.ltzin.database.data.DataTable;
import org.ltzin.database.data.PreferencesContainer;
import org.ltzin.database.data.interfaces.AbstractContainer;
import org.ltzin.database.data.interfaces.DataTableInfo;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.game.Game;
import org.ltzin.game.GameTeam;
import org.ltzin.hotbar.Hotbar;
import org.ltzin.player.role.Role;
import org.ltzin.player.scoreboard.Score;
import org.ltzin.skin.SkinData;
import org.ltzin.utils.BukkitUtils;
import org.ltzin.utils.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Profile {

    private static final Map<String, Profile> PROFILES = new ConcurrentHashMap<>();
    private Score scoreboard;
    private String name;
    private Player player;
    private Map<String, Map<String, DataContainer>> tableMap;
    private Hotbar hotbar;
    private Game<? extends GameTeam> game;
    private Map<String, Long> lastHit = new HashMap<>();

    private static final Map<String, List<String>> SET_COLUMNS_CACHE = new ConcurrentHashMap<>();

    private volatile SkinData pendingSkin;

    private volatile StorageImplementation storage;
    private final Object saveLock = new Object();
    private volatile boolean saveScheduled = false;

    private Profile(String name) {
        this.name     = name;
        this.tableMap = new HashMap<>();
    }

    public static Profile getProfile(String playerName) {
        if (playerName == null) return null;
        return PROFILES.get(playerName.toLowerCase());
    }

    public static Profile getProfile(Player player) {
        if (player == null) return null;
        return getProfile(player.getName());
    }

    public static boolean isLoaded(String playerName) {
        if (playerName == null) return false;
        return PROFILES.containsKey(playerName.toLowerCase());
    }

    public static Collection<Profile> listProfiles() {
        return Collections.unmodifiableCollection(PROFILES.values());
    }


    public static Profile load(String playerName, StorageImplementation storage) throws SQLException {
        String key = playerName.toLowerCase();

        Profile existing = PROFILES.get(key);
        if (existing != null) return existing;

        Profile profile = new Profile(playerName);
        profile.storage = storage;

        try (Connection conn = storage.getConnection()) {
            for (DataTable table : DataTable.listTables()) {
                DataTableInfo info = table.getInfo();
                Map<String, DataContainer> row = profile.loadRow(conn, table, info, playerName);
                profile.tableMap.put(info.name(), row);
            }
        }

        DataContainer lastLogin = profile.getDataContainer("VoidProfile", "lastlogin");
        if (lastLogin != null) {
            lastLogin.set(System.currentTimeMillis());
        }

        Profile winner = PROFILES.putIfAbsent(key, profile);
        return winner != null ? winner : profile;
    }

    private Map<String, DataContainer> loadRow(Connection conn,
                                               DataTable table,
                                               DataTableInfo info,
                                               String playerName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(info.select())) {
            ps.setString(1, playerName.toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, DataContainer> row = new LinkedHashMap<>();
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();

                    for (int i = 1; i <= cols; i++) {
                        String col = meta.getColumnName(i);
                        if (col.equalsIgnoreCase("name")) continue;
                        Object val = rs.getObject(i);
                        row.put(col, new DataContainer(val != null ? val : ""));
                    }

                    if (table != null) {
                        for (Map.Entry<String, DataContainer> def : table.getDefaultValues().entrySet()) {
                            row.putIfAbsent(def.getKey(), new DataContainer(def.getValue().get()));
                        }
                    }

                    return row;

                } else {
                    Map<String, DataContainer> defaults = table != null
                            ? table.getDefaultValues()
                            : new LinkedHashMap<>();

                    insertRow(conn, info, playerName, defaults);
                    return deepCopy(defaults);
                }
            }
        }
    }

    private void insertRow(Connection conn,
                           DataTableInfo info,
                           String playerName,
                           Map<String, DataContainer> defaults) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(info.insert())) {
            ps.setString(1, playerName);
            int idx = 2;
            for (DataContainer dc : defaults.values()) {
                ps.setObject(idx++, dc.get());
            }
            ps.executeUpdate();
        }
    }


    public void save(StorageImplementation storage) {
        if (name == null || tableMap == null) return;

        boolean anyDirty = false;
        for (DataTable table : DataTable.listTables()) {
            Map<String, DataContainer> row = tableMap.get(table.getInfo().name());
            if (row != null && row.values().stream().anyMatch(DataContainer::isUpdated)) {
                anyDirty = true;
                break;
            }
        }
        if (!anyDirty) return;

        try (Connection conn = storage.getConnection()) {
            for (DataTable table : DataTable.listTables()) {
                DataTableInfo info = table.getInfo();
                Map<String, DataContainer> row = tableMap.get(info.name());
                if (row == null) continue;

                boolean anyUpdated = row.values().stream().anyMatch(DataContainer::isUpdated);
                if (!anyUpdated) continue;

                try (PreparedStatement ps = conn.prepareStatement(info.update())) {
                    List<String> setCols = getCachedSetColumns(info.update());
                    int idx = 1;
                    for (String col : setCols) {
                        DataContainer dc = row.get(col);
                        ps.setObject(idx++, dc != null ? dc.get() : null);
                    }
                    ps.setString(idx, name.toLowerCase());
                    ps.executeUpdate();

                    row.values().forEach(dc -> dc.setUpdated(false));

                } catch (SQLException ex) {
                    DataTable.LOGGER.warning("[VoidlessProfile] Erro ao salvar '" + name + "' em "
                            + info.name() + ": " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            DataTable.LOGGER.warning("[VoidlessProfile] Erro ao obter conexão para save de '"
                    + name + "': " + ex.getMessage());
        }
    }

    public void saveSync(StorageImplementation storage) {
        save(storage);
    }


    public void requestSave() {
        StorageImplementation storage = this.storage;
        if (storage == null) return;

        synchronized (saveLock) {
            if (saveScheduled) return;
            saveScheduled = true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                save(storage);
            } finally {
                saveScheduled = false;
            }
        });
    }

    public void refresh() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }

        // Reset básico
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExhaustion(0.0f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.closeInventory();

        // Remover efeitos
        for (PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
        }

        if (!playingGame()) {
            player.setGameMode(GameMode.ADVENTURE);
            Role playerRole = Role.byName(Role.getRole(player));

            Location spawnLocation = Main.getLobby().clone();
            if (playerRole.canFly()) {
                spawnLocation.add(0.0F, 6.0F, 0.0F);
            }

            player.teleport(spawnLocation);

            player.setAllowFlight(false);
            player.setFlying(false);

            // Delay para ativar fly se tiver permissão
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (player.isOnline() && playerRole.canFly()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }, 1L);

            this.getDataContainer("VoidProfile", "role").set(StringUtils.stripColors(Role.getRole(player)));
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (this.hotbar != null) {
            this.hotbar.apply(this);
        }

        this.refreshPlayers();
    }

    public void setHit(String name) {
        this.lastHit.put(name, System.currentTimeMillis() + 8000L);
    }

    public List<Profile> getLastHitters() {
        long now = System.currentTimeMillis();

        List<Profile> hitters = lastHit.entrySet().stream()
                .filter(entry -> entry.getValue() > now)
                .map(entry -> getProfile(entry.getKey()))
                .filter(Objects::nonNull)
                .filter(Profile::isOnline)
                .sorted(Comparator.comparing(
                        profile -> lastHit.get(profile.getName()),
                        Comparator.reverseOrder())).collect(Collectors.toList());

        lastHit.clear();

        return hitters;
    }


    public static Profile unload(String playerName) {
        if (playerName == null) return null;
        return PROFILES.remove(playerName.toLowerCase());
    }
    public void refreshPlayers() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }

        if (this.hotbar != null) {
            this.hotbar.getButtons().stream()
                    .filter(button -> button.getAction().getValue().equalsIgnoreCase("jogadores"))
                    .forEach(button -> player.getInventory().setItem(
                            button.getSlot(),
                            BukkitUtils.deserializeItemStack(PlaceholderAPI.setPlaceholders(player, button.getIcon()))
                    ));
        }
    }

    public void destroy() {
        this.name        = null;
        this.player      = null;
        this.pendingSkin = null;
        this.hotbar      = null;
        this.scoreboard = null;
        this.game = null;
        this.storage = null;
        if (this.lastHit != null) {
            this.lastHit.clear();
            this.lastHit = null;
        }
        if (this.tableMap != null) {
            this.tableMap.values().forEach(row -> {
                row.values().forEach(DataContainer::gc);
                row.clear();
            });
            this.tableMap.clear();
            this.tableMap = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Game<?>> T getGame(Class<T> gameClass) {
        return this.game != null && gameClass.isAssignableFrom(this.game.getClass()) ? (T) this.game : null;
    }
    public Game<?> getGame() {
        return this.getGame(Game.class);
    }

    public void setGame(Game<? extends GameTeam> game) {
        this.game = game;
        this.lastHit.clear();
    }


    public boolean playingGame() {
        return this.game != null;
    }
    public void setPendingSkin(SkinData skin) {
        this.pendingSkin = skin;
    }

    public SkinData consumePendingSkin() {
        SkinData skin    = this.pendingSkin;
        this.pendingSkin = null;
        return skin;
    }

    public boolean hasPendingSkin() {
        return pendingSkin != null;
    }

    private static final String SKIN_DELIMITER = ";;";

    public void setSkinData(SkinData skin) {
        DataContainer dc = getDataContainer("VoidProfile", "skin");
        if (dc == null) return;

        if (skin == null || skin.getValue() == null) {
            dc.set("");
            return;
        }

        String signature = skin.getSignature() != null ? skin.getSignature() : "";
        dc.set(skin.getValue() + SKIN_DELIMITER + signature);
    }

    public SkinData getSkinData() {
        DataContainer dc = getDataContainer("VoidProfile", "skin");
        if (dc == null) return null;

        String raw = dc.getAsString();
        if (raw == null || raw.isEmpty()) return null;

        int idx = raw.indexOf(SKIN_DELIMITER);
        if (idx == -1) return new SkinData(raw, null);

        String value     = raw.substring(0, idx);
        String signature = raw.substring(idx + SKIN_DELIMITER.length());
        return new SkinData(value, signature.isEmpty() ? null : signature);
    }

    public boolean hasCustomSkin() {
        DataContainer dc = getDataContainer("VoidProfile", "skin");
        if (dc == null) return false;
        String raw = dc.getAsString();
        return raw != null && !raw.isEmpty();
    }

    public DataContainer getDataContainer(String tableName, String key) {
        if (tableMap == null) return null;
        Map<String, DataContainer> row = tableMap.get(tableName);
        if (row == null) return null;
        return row.get(key);
    }

    public String getSelectedTag() {
        DataContainer dc = getDataContainer("VoidProfile", "selected_tag");
        return dc != null ? dc.getAsString() : "";
    }

    public void setSelectedTag(String tagId) {
        DataContainer dc = getDataContainer("VoidProfile", "selected_tag");
        if (dc != null) dc.set(tagId != null ? tagId : "");
    }

    public PreferencesContainer getPreferences() {
        DataContainer dc = getDataContainer("VoidProfile", "preferences");
        if (dc == null) throw new IllegalStateException("DataContainer 'preferences' não encontrado.");
        return dc.getContainer(PreferencesContainer.class);
    }

    public <T extends AbstractContainer> T getContainer(String tableName, String key, Class<T> containerClass) {
        DataContainer dc = getDataContainer(tableName, key);
        if (dc == null) throw new IllegalStateException(
                "DataContainer não encontrado: " + tableName + "." + key);
        return dc.getContainer(containerClass);
    }

    public long getStats(String tableName, String... keys) {
        long total = 0;
        for (String key : keys) {
            DataContainer dc = getDataContainer(tableName, key);
            if (dc != null) total += dc.getAsLong();
        }
        return total;
    }

    public void addStats(String tableName, long amount, String... keys) {
        for (String key : keys) {
            DataContainer dc = getDataContainer(tableName, key);
            if (dc != null) dc.addLong(amount);
        }
        requestSave();
    }

    public void addStats(String tableName, String... keys) {
        addStats(tableName, 1L, keys);
    }

    public void setStats(String tableName, long amount, String... keys) {
        for (String key : keys) {
            DataContainer dc = getDataContainer(tableName, key);
            if (dc != null) dc.set(amount);
        }
        requestSave();
    }

    public long getCash() {
        DataContainer dc = getDataContainer("VoidProfile", "cash");
        return dc != null ? dc.getAsLong() : 0L;
    }

    public void setCash(long value) {
        DataContainer dc = getDataContainer("VoidProfile", "cash");
        if (dc != null) dc.set(value);
        requestSave();
    }

    public void addCash(long amount) {
        DataContainer dc = getDataContainer("VoidProfile", "cash");
        if (dc != null) dc.addLong(amount);
        requestSave();
    }

    public void removeCash(long amount) {
        DataContainer dc = getDataContainer("VoidProfile", "cash");
        if (dc != null) dc.removeLong(amount);
        requestSave();
    }

    public String getRole() {
        DataContainer dc = getDataContainer("VoidProfile", "role");
        return dc != null ? dc.getAsString() : "Membro";
    }

    public void setRole(String role) {
        DataContainer dc = getDataContainer("VoidProfile", "role");
        if (dc != null) dc.set(role);
        requestSave();
    }

    public long getCreated() {
        DataContainer dc = getDataContainer("VoidProfile", "created");
        return dc != null ? dc.getAsLong() : 0L;
    }

    public long getLastLogin() {
        DataContainer dc = getDataContainer("VoidProfile", "lastlogin");
        return dc != null ? dc.getAsLong() : 0L;
    }

    public String getName() {
        return name;
    }

    public void addCoins(String table, double amount) {
        if (Main.minigame.equals("Sky Wars")) {
            this.getDataContainer(table, "coins").addDouble(amount);
            requestSave();
            return;
        }

        double currentCoins = this.getCoins(table);
        double newTotal = currentCoins + amount;

        this.getDataContainer(table, "coins").addDouble(amount);
        requestSave();
    }

    public int addCoinsWM(String table, double amount) {
        amount = this.calculateWM(amount);
        this.addCoins(table, amount);
        return (int) amount;
    }

    public double getCoins(String table) {
        return this.getDataContainer(table, "coins").getAsDouble();
    }

    public double calculateWM(double amount) {
        double add = 0.0D;
//        String booster = this.getBoostersContainer().getEnabled();
//        if (booster != null) {
//            add = amount * Double.parseDouble(booster.split(":")[0]);
//        }
//
//        NetworkBooster nb = Booster.getNetworkBooster(Core.minigame);
//        if (nb != null) {
//            add += amount * nb.getMultiplier();
//        }

        return (amount > 0.0 && add == 0.0) ? amount : add;
    }

    public void removeCoins(String table, double amount) {
        this.getDataContainer(table, "coins").removeDouble(amount);
        requestSave();
    }
    public Player getPlayer() {
        if (player != null && player.isOnline()) return player;
        if (name != null) {
            player = Bukkit.getPlayerExact(name);
        }
        return player;
    }
    public Score getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(Score scoreboard) {
        if (this.scoreboard != null) {
            this.scoreboard.destroy();
        }

        this.scoreboard = scoreboard;

        Player player = this.getPlayer();
        if (player != null && scoreboard != null) {
            player.setScoreboard(scoreboard.getScoreboard());
        }
    }

    public void update() {
        try {
            if (this.scoreboard != null) {
                this.scoreboard.update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isOnline() {
        return name != null && PROFILES.containsKey(name);
    }

    public Hotbar getHotbar() {
        return this.hotbar;
    }

    public void setHotbar(Hotbar hotbar) {
        this.hotbar = hotbar;
        Player p = this.getPlayer();
        Profile profile = getProfile(p.getName());
        if (hotbar != null && profile != null) {
            hotbar.apply(profile);
        }
    }

    public Map<String, Map<String, DataContainer>> getTableMap() {
        return tableMap;
    }

    private static DataTable findTable(String tableName) {
        for (DataTable t : DataTable.listTables()) {
            if (t.getInfo().name().equalsIgnoreCase(tableName)) return t;
        }
        return null;
    }

    private static List<String> getCachedSetColumns(String updateSQL) {
        return SET_COLUMNS_CACHE.computeIfAbsent(updateSQL, Profile::parseSetColumns);
    }

    private static List<String> parseSetColumns(String updateSQL) {
        List<String> cols = new ArrayList<>();
        int setIdx   = updateSQL.toUpperCase().indexOf("SET");
        int whereIdx = updateSQL.toUpperCase().indexOf("WHERE");
        if (setIdx == -1 || whereIdx == -1) return cols;

        String setPart = updateSQL.substring(setIdx + 3, whereIdx).trim();
        for (String token : setPart.split(",")) {
            token = token.trim();
            int eq = token.indexOf('=');
            if (eq == -1) continue;
            String col = token.substring(0, eq).trim()
                    .replace("`", "")
                    .trim();
            cols.add(col);
        }
        return cols;
    }

    private static Map<String, DataContainer> deepCopy(Map<String, DataContainer> source) {
        Map<String, DataContainer> copy = new LinkedHashMap<>();
        for (Map.Entry<String, DataContainer> e : source.entrySet()) {
            copy.put(e.getKey(), new DataContainer(e.getValue().get()));
        }
        return copy;
    }
}