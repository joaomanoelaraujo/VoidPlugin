package org.ltzin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ltzin.Main;
import org.ltzin.api.VoidlessAPI;
import org.ltzin.database.data.DataContainer;
import org.ltzin.database.tables.VoidlessTable;
import org.ltzin.logger.VLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoidlessListeners implements Listener {

    private static final VLogger LOGGER = Main.getInstance().getMyLogger();

    private static final Map<String, Map<String, DataContainer>> PROFILE_CACHE = new ConcurrentHashMap<>();

    public static void setup() {
        Main.getInstance().getServer().getPluginManager()
                .registerEvents(new VoidlessListeners(), Main.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent evt) {
        if (evt.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        String playerName = evt.getName();

        try {
            // load() já faz INSERT com defaults se for jogador novo
            Map<String, DataContainer> data = VoidlessAPI.get()
                    .players()
                    .load(playerName, VoidlessTable.class);

            if (data == null) {
                throw new RuntimeException("Retorno nulo ao carregar dados de " + playerName);
            }

            PROFILE_CACHE.put(playerName.toLowerCase(), data);
            LOGGER.info("Perfil carregado: " + playerName);

        } catch (Exception ex) {
            LOGGER.warning("Falha ao carregar perfil de " + playerName + ": " + ex.getMessage());
            ex.printStackTrace();

            evt.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "§cFalha ao carregar seu perfil. Tente novamente."
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent evt) {
        String playerName = evt.getPlayer().getName();

        if (!PROFILE_CACHE.containsKey(playerName.toLowerCase())) {
            evt.disallow(
                PlayerLoginEvent.Result.KICK_OTHER,
                "§cSeu perfil não pôde ser carregado.\n§cIsso ocorre quando o servidor ainda não está pronto. Tente novamente."
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent evt) {
        Player player = evt.getPlayer();
        String playerName = player.getName();

        Map<String, DataContainer> data = PROFILE_CACHE.remove(playerName.toLowerCase());
        if (data == null) {
            return;
        }

        Main.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(Main.getInstance(), () -> {
                    try {
                        VoidlessAPI.get().players().save(playerName, data, VoidlessTable.class);
                        LOGGER.info("Perfil salvo: " + playerName);
                    } catch (Exception ex) {
                        LOGGER.warning("Falha ao salvar perfil de " + playerName + ": " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
    }


    public static Map<String, DataContainer> getProfile(String playerName) {
        return PROFILE_CACHE.get(playerName.toLowerCase());
    }

    public static DataContainer get(String playerName, String field) {
        Map<String, DataContainer> profile = getProfile(playerName);
        return profile != null ? profile.get(field) : null;
    }

    public static boolean isLoaded(String playerName) {
        return PROFILE_CACHE.containsKey(playerName.toLowerCase());
    }
}