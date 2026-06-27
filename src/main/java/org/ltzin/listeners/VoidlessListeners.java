package org.ltzin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ltzin.Main;
import org.ltzin.logger.VLogger;
import org.ltzin.player.Profile;
import org.ltzin.skin.SkinApplier;
import org.ltzin.skin.SkinData;
import org.ltzin.skin.SkinFetcher;

public class VoidlessListeners implements Listener {

    private static final VLogger LOGGER = Main.getInstance().getMyLogger();

    private static final SkinFetcher SKIN_FETCHER = new SkinFetcher(Main.getInstance().getLogger());
    private static final SkinApplier SKIN_APPLIER = new SkinApplier(Main.getInstance().getLogger());

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
            Profile profile = Profile.load(playerName, Main.getInstance().getStorage());

            if (profile == null) {
                throw new RuntimeException("Retorno nulo ao carregar perfil de " + playerName);
            }

            LOGGER.info("Perfil carregado: " + playerName);

            SkinData skin = SKIN_FETCHER.fetch(playerName);

            if (skin != null) {
                profile.setPendingSkin(skin);
                LOGGER.info("Skin obtida para: " + playerName);
            } else {
                LOGGER.info("Sem skin Mojang para: " + playerName + " (nick não encontrado ou sem conta premium)");
            }

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
        Player  player  = evt.getPlayer();
        Profile profile = Profile.getProfile(player.getName());

        if (profile == null) {
            evt.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    "§cSeu perfil não pôde ser carregado.\n§cIsso ocorre quando o servidor ainda não está pronto. Tente novamente."
            );
            return;
        }

        profile.setPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player  player  = evt.getPlayer();
        Profile profile = Profile.getProfile(player.getName());

        if (profile == null || !profile.hasPendingSkin()) {
            return;
        }

        SkinData skin = profile.consumePendingSkin();
        SKIN_APPLIER.apply(player, skin);
        LOGGER.info("Skin aplicada: " + player.getName());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent evt) {
        String  playerName = evt.getPlayer().getName();
        Profile profile    = Profile.getProfile(playerName);

        if (profile == null) {
            return;
        }

        Main.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(Main.getInstance(), () -> {
                    try {
                        profile.save(Main.getInstance().getStorage());
                        LOGGER.info("Perfil salvo: " + playerName);
                    } catch (Exception ex) {
                        LOGGER.warning("Falha ao salvar perfil de " + playerName + ": " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        Profile.unload(playerName);
                    }
                });
    }

    public static SkinFetcher getSkinFetcher() {
        return SKIN_FETCHER;
    }

    public static SkinApplier getSkinApplier() {
        return SKIN_APPLIER;
    }
}