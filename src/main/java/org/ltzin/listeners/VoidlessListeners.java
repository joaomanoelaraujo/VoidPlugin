package org.ltzin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.ltzin.Main;
import org.ltzin.hotbar.HotbarButton;
import org.ltzin.logger.VLogger;
import org.ltzin.player.Profile;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;
import org.ltzin.skin.SkinApplier;
import org.ltzin.skin.SkinData;
import org.ltzin.skin.SkinFetcher;
import org.ltzin.skin.SkinLibrary;
import org.ltzin.utils.StringUtils;
import org.ltzin.utils.VisibilityUtils;

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
        if (evt.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        String playerName = evt.getName();
        boolean isPremium = evt.getUniqueId().version() == 4;

        try {
            Profile profile = Profile.load(playerName, Main.getInstance().getStorage());

            LOGGER.info("Perfil carregado: " + playerName);

            if (profile.hasCustomSkin()) {
                profile.setPendingSkin(profile.getSkinData());
                LOGGER.info("Skin customizada restaurada para: " + playerName);
            } else if (isPremium) {
                SkinData mojangSkin = SKIN_FETCHER.fetch(playerName);
                if (mojangSkin != null) {
                    profile.setPendingSkin(mojangSkin);
                    LOGGER.info("Skin Mojang obtida para: " + playerName);
                } else {
                    profile.setPendingSkin(SkinLibrary.getDefaultSkin());
                    LOGGER.info("Conta premium sem skin encontrada: aplicando skin padrão para: " + playerName);
                }
            } else {
                profile.setPendingSkin(SkinLibrary.getDefaultSkin());
                LOGGER.info("Conta não original: aplicando skin padrão para: " + playerName);
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
                    "§cSeu perfil não pôde ser carregado.\n§cIsso ocorre quando o servidor ainda"
                            + " não está pronto. Tente novamente."
            );
            return;
        }

        profile.setPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player  player  = evt.getPlayer();
        Profile profile = Profile.getProfile(player.getName());

        evt.setJoinMessage(null);

        if (!Role.isReady()) {
            LOGGER.warning("Ranks não registrados ao processar join de " + player.getName());
            return;
        }

        if (profile != null) {
            syncRoleFromPermission(player, profile);
        }

        if (profile != null && profile.hasPendingSkin()) {
            final SkinData skin = profile.consumePendingSkin();
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!player.isOnline()) return;
                SKIN_APPLIER.apply(player, skin);
                LOGGER.info("Skin aplicada: " + player.getName());
            }, 4L);
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!player.isOnline()) return;
            VisibilityUtils.updateVisibility(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent evt) {
        String  playerName = evt.getPlayer().getName();

        Profile profile = Profile.unload(playerName);

        if (profile == null) return;

        Main.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(Main.getInstance(), () -> {
                    try {
                        profile.save(Main.getInstance().getStorage());
                        LOGGER.info("Perfil salvo: " + playerName);
                    } catch (Exception ex) {
                        LOGGER.warning("Falha ao salvar perfil de " + playerName
                                + ": " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        profile.destroy();
                    }
                });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player player = evt.getPlayer();
        Profile profile = Profile.getProfile(player.getName());

        if (profile == null || profile.getHotbar() == null) {
            return;
        }

        ItemStack item = player.getItemInHand();
        if (evt.getAction().name().contains("CLICK") && item != null && item.hasItemMeta()) {
            HotbarButton button = profile.getHotbar().compareButton(profile, item);
            if (button != null) {
                evt.setCancelled(true);
                button.getAction().execute(profile);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent evt) {
        if (!(evt.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) evt.getWhoClicked();
        Profile profile = Profile.getProfile(player.getName());

        if (profile == null || profile.getHotbar() == null ||
                evt.getClickedInventory() == null || !evt.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        ItemStack item = evt.getCurrentItem();
        if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
            HotbarButton button = profile.getHotbar().compareButton(profile, item);
            if (button != null) {
                evt.setCancelled(true);
                button.getAction().execute(profile);
            }
        }
    }

    private void syncRoleFromPermission(Player player, Profile profile) {
        Role detected     = Role.byPermission(player);
        String detectedName = StringUtils.stripColors(detected.getName());
        String savedRole    = profile.getRole();

        if (!detectedName.equalsIgnoreCase(savedRole)) {
            profile.setRole(detectedName);
            LOGGER.info("Role sincronizado: " + player.getName()
                    + " [" + savedRole + " → " + detectedName + "]");
        }
    }

    public static SkinFetcher getSkinFetcher() { return SKIN_FETCHER; }
    public static SkinApplier getSkinApplier() { return SKIN_APPLIER; }
}