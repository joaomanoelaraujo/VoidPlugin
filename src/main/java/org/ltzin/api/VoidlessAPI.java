package org.ltzin.api;


import org.ltzin.api.database.StorageAPI;
import org.ltzin.api.player.PlayerDataAPI;
import org.ltzin.api.table.TableAPI;

public final class VoidlessAPI {

    private static VoidlessAPI instance;

    private final StorageAPI storageAPI;
    private final TableAPI tableAPI;
    private final PlayerDataAPI playerDataAPI;

    public VoidlessAPI(StorageAPI storageAPI, TableAPI tableAPI, PlayerDataAPI playerDataAPI) {
        this.storageAPI    = storageAPI;
        this.tableAPI      = tableAPI;
        this.playerDataAPI = playerDataAPI;
    }
 
    public static VoidlessAPI get() {
        if (instance == null) {
            throw new IllegalStateException("VoidlessAPI ainda não foi inicializada. O VoidlessPlugin está carregado?");
        }
        return instance;
    }

    public static void init(VoidlessAPI api) {
        if (instance != null) {
            throw new IllegalStateException("VoidlessAPI já foi inicializada.");
        }
        instance = api;
    }

    public static void shutdown() {
        instance = null;
    }

    public StorageAPI storage()    { return storageAPI; }
    public TableAPI tables()       { return tableAPI; }
    public PlayerDataAPI players() { return playerDataAPI; }
}