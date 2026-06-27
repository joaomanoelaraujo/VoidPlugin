package org.ltzin.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.ltzin.Main;
import org.ltzin.database.data.DataContainer;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.libraries.PlayerMenu;
import org.ltzin.menus.category.MenuCategory;
import org.ltzin.player.Profile;
import org.ltzin.player.enums.*;
import org.ltzin.utils.BukkitUtils;

public class MenuPreferences extends PlayerMenu {

    private final Profile profile;
    private final MenuCategory category;

    private static final int SLOT_CAT_INGAME  = 2;
    private static final int SLOT_CAT_CHAT    = 3;
    private static final int SLOT_CAT_SOCIALS = 4;
    private static final int SLOT_CAT_LOBBY   = 5;
    private static final int SLOT_CAT_GUILD   = 6;

    private static final int[] SLOTS_GLASS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    private static final int ROW_ICON   = 20;
    private static final int ROW_TOGGLE = 29;

    private static final String GUILD_SKIN =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmIwMjkyNDQwNDJlZTY5NDFkMDEwNzU1ZTMwYWI1ZjRkMDk1YTMwZmE4MGZmNThiYmE2N2U4NWNhNWI4OTU3ZCJ9fX0=";

    public MenuPreferences(Profile profile) {
        this(profile, MenuCategory.INGAME);
    }

    public MenuPreferences(Profile profile, MenuCategory category) {
        super(profile.getPlayer(), "Preferências » " + category.getValue(), 6);
        this.profile  = profile;
        this.category = category;

        buildMenu();

        this.register(Main.getInstance());
        this.open();
    }

    private void buildMenu() {
        buildCategories();
        buildSeparator();
        buildPreferences();
        buildFooter();
    }

    private void buildCategories() {
        this.setItem(SLOT_CAT_INGAME, BukkitUtils.deserializeItemStack(
                "404 : 1 : name>§aConfigurações do jogo"
                        + (category == MenuCategory.INGAME ? "" : " : desc> §7> §eClique para ver! §7<")));

        this.setItem(SLOT_CAT_CHAT, BukkitUtils.deserializeItemStack(
                "PAPER : 1 : name>§aConfigurações do chat"
                        + (category == MenuCategory.CHAT ? "" : " : desc> §7> §eClique para ver! §7<")));

        this.setItem(SLOT_CAT_SOCIALS, BukkitUtils.deserializeItemStack(
                "SIGN : 1 : name>§aConfigurações sociais"
                        + (category == MenuCategory.SOCIALS ? "" : " : desc> §7> §eClique para ver! §7<")));

        this.setItem(SLOT_CAT_LOBBY, BukkitUtils.deserializeItemStack(
                "NETHER_STAR : 1 : name>§aConfigurações do lobby"
                        + (category == MenuCategory.LOBBY ? "" : " : desc> §7> §eClique para ver! §7<")));

        this.setItem(SLOT_CAT_GUILD, BukkitUtils.deserializeItemStack(
                "SKULL_ITEM:3 : 1 : name>§aConfigurações de guilda"
                        + (category == MenuCategory.GUILD ? "" : " : desc> §7> §eClique para ver! §7<")
                        + " : skin>" + GUILD_SKIN));
    }

    private void buildSeparator() {
        ItemStack glass = BukkitUtils.deserializeItemStack(
                "160:7 : 1 : name>§8⬆ §7Categorias : desc>§8⬇ §7Configurações");
        for (int slot : SLOTS_GLASS) {
            this.setItem(slot, glass);
        }
    }

    private void buildPreferences() {
        switch (category) {
            case INGAME:  buildIngame();  break;
            case CHAT:    buildChat();    break;
            case SOCIALS: buildSocials(); break;
            case LOBBY:   buildLobby();   break;
            case GUILD:   buildGuild();   break;
        }
    }

    private void setPreferenceItem(int index, ItemStack iconItem, ItemStack toggleItem) {
        this.setItem(ROW_ICON   + index, iconItem);
        this.setItem(ROW_TOGGLE + index, toggleItem);
    }

    private int toggleSlot(int index) { return ROW_TOGGLE + index; }

    private void buildIngame() {
        AutoQueue      aq = getEnum("aq", AutoQueue.class,      AutoQueue.NENHUM);
        MapRating      mr = getEnum("mr", MapRating.class,      MapRating.TODOS);
        BloodParticles bp = getEnum("bp", BloodParticles.class, BloodParticles.TODOS);
        MapSelector    ms = getEnum("ms", MapSelector.class,    MapSelector.TODOS);

        setPreferenceItem(0,
                BukkitUtils.deserializeItemStack(
                        "BED : 1"
                                + " : name>§aFila Automática"
                                + " : desc>§7Ao terminar uma partida, entra\n§7automaticamente em uma nova fila."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + aq.getInkSack() + " : 1"
                                + " : name>" + aq.getColor() + "Fila Automática: " + aq.getName()
                                + " : desc>§7Atual: " + aq.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(1,
                BukkitUtils.deserializeItemStack(
                        "MAP : 1"
                                + " : name>§aAvaliação de Mapas"
                                + " : desc>§7Receba questionários de avaliação\n§7dos mapas jogados."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + mr.getInkSack() + " : 1"
                                + " : name>" + mr.getColor() + "Avaliação de Mapas: " + mr.getName()
                                + " : desc>§7Atual: " + mr.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(2,
                BukkitUtils.deserializeItemStack(
                        "REDSTONE : 1"
                                + " : name>§aSangue"
                                + " : desc>§7Exibe partículas de sangue\n§7durante o combate."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + bp.getInkSack() + " : 1"
                                + " : name>" + bp.getColor() + "Sangue: " + bp.getName()
                                + " : desc>§7Atual: " + bp.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(3,
                BukkitUtils.deserializeItemStack(
                        "COMPASS : 1"
                                + " : name>§aSeletor de Mapas"
                                + " : desc>§7Participe da votação de mapas\n§7antes das partidas."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + ms.getInkSack() + " : 1"
                                + " : name>" + ms.getColor() + "Seletor de Mapas: " + ms.getName()
                                + " : desc>§7Atual: " + ms.getName()
                                + "\n\n§eClique para alternar!"));
    }

    private void buildChat() {
        Mentions          mn = getEnum("mn", Mentions.class,          Mentions.TODOS);
        ChatMessages      ch = getEnum("ch", ChatMessages.class,      ChatMessages.TODOS);
        WordFilter        wf = getEnum("wf", WordFilter.class,        WordFilter.TODOS);
        LobbyJoinMessages lm = getEnum("lm", LobbyJoinMessages.class, LobbyJoinMessages.TODOS);

        setPreferenceItem(0,
                BukkitUtils.deserializeItemStack(
                        "NAME_TAG : 1"
                                + " : name>§aMenções"
                                + " : desc>§7Receba uma notificação sempre que\n§7alguém te mencionar no chat."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + mn.getInkSack() + " : 1"
                                + " : name>" + mn.getColor() + "Menções: " + mn.getName()
                                + " : desc>§7Atual: " + mn.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(1,
                BukkitUtils.deserializeItemStack(
                        "BOOK : 1"
                                + " : name>§aChat"
                                + " : desc>§7Define se você recebe mensagens\n§7de outros jogadores no chat."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + ch.getInkSack() + " : 1"
                                + " : name>" + ch.getColor() + "Chat: " + ch.getName()
                                + " : desc>§7Atual: " + ch.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(2,
                BukkitUtils.deserializeItemStack(
                        "PAPER : 1"
                                + " : name>§aFiltro de Palavrões"
                                + " : desc>§7Filtra palavras de baixo calão\n§7no chat."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + wf.getInkSack() + " : 1"
                                + " : name>" + wf.getColor() + "Filtro de Palavrões: " + wf.getName()
                                + " : desc>§7Atual: " + wf.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(3,
                BukkitUtils.deserializeItemStack(
                        "WOOD_DOOR : 1"
                                + " : name>§aEntrada no Lobby"
                                + " : desc>§7Exibe mensagens de entrada\n§7de jogadores no lobby."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + lm.getInkSack() + " : 1"
                                + " : name>" + lm.getColor() + "Entrada no Lobby: " + lm.getName()
                                + " : desc>§7Atual: " + lm.getName()
                                + "\n\n§eClique para alternar!"));
    }

    private void buildSocials() {
        PrivateMessages pm = getEnum("pm", PrivateMessages.class, PrivateMessages.TODOS);

        setPreferenceItem(0,
                BukkitUtils.deserializeItemStack(
                        "PAPER : 1"
                                + " : name>§aMensagens Privadas"
                                + " : desc>§7Permite ou bloqueia o recebimento\n§7de mensagens privadas."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + pm.getInkSack() + " : 1"
                                + " : name>" + pm.getColor() + "Mensagens Privadas: " + pm.getName()
                                + " : desc>§7Atual: " + pm.getName()
                                + "\n\n§eClique para alternar!"));
    }

    private void buildLobby() {
        PlayerVisibility pv  = getEnum("pv",  PlayerVisibility.class, PlayerVisibility.TODOS);
        TimeOfDay        td  = getEnum("td",  TimeOfDay.class,        TimeOfDay.DIA);
        FlyOnJoin        fly = getEnum("fly", FlyOnJoin.class,        FlyOnJoin.NENHUM);

        setPreferenceItem(0,
                BukkitUtils.deserializeItemStack(
                        "WATCH : 1"
                                + " : name>§aTempo do Dia"
                                + " : desc>§7Altera o horário do dia no lobby,\n§7visível apenas para você."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + td.getInkSack() + " : 1"
                                + " : name>" + td.getColor() + "Tempo: " + td.getName()
                                + " : desc>§7Atual: " + td.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(1,
                BukkitUtils.deserializeItemStack(
                        "GLASS : 1"
                                + " : name>§aVisualizar Jogadores"
                                + " : desc>§7Ativa ou desativa a visibilidade\n§7de outros jogadores no lobby."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + pv.getInkSack() + " : 1"
                                + " : name>" + pv.getColor() + "Visualizar Jogadores: " + pv.getName()
                                + " : desc>§7Atual: " + pv.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(2,
                BukkitUtils.deserializeItemStack(
                        "FEATHER : 1"
                                + " : name>§aVoar ao Entrar"
                                + " : desc>§7Entra no lobby já voando\n§7automaticamente."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + fly.getInkSack() + " : 1"
                                + " : name>" + fly.getColor() + "Voar ao Entrar: " + fly.getName()
                                + " : desc>§7Atual: " + fly.getName()
                                + "\n\n§eClique para alternar!"));
    }


    private void buildGuild() {
        GuildChat          gc = getEnum("gc", GuildChat.class,          GuildChat.TODOS);
        GuildNotifications gn = getEnum("gn", GuildNotifications.class, GuildNotifications.TODOS);

        setPreferenceItem(0,
                BukkitUtils.deserializeItemStack(
                        "GOLD_INGOT : 1"
                                + " : name>§aChat da Guilda"
                                + " : desc>§7Define a visibilidade das mensagens\n§7do chat da sua guilda."),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + gc.getInkSack() + " : 1"
                                + " : name>" + gc.getColor() + "Chat da Guilda: " + gc.getName()
                                + " : desc>§7Atual: " + gc.getName()
                                + "\n\n§eClique para alternar!"));

        setPreferenceItem(1,
                BukkitUtils.deserializeItemStack(
                        "SKULL_ITEM:3 : 1"
                                + " : name>§aNotificações de Guilda"
                                + " : desc>§7Receba notificações de eventos\n§7da sua guilda."
                                + " : skin>eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZiMGNlNjczYjNmMjhjNDYxMGNlYTdjZTA0MmM4NTBlMzRjYzk4OGNiMGQ3YzgwMzk3OWY1MGRkMGYxNTczMSJ9fX0="),
                BukkitUtils.deserializeItemStack(
                        "INK_SACK:" + gn.getInkSack() + " : 1"
                                + " : name>" + gn.getColor() + "Notificações de Guilda: " + gn.getName()
                                + " : desc>§7Atual: " + gn.getName()
                                + "\n\n§eClique para alternar!"));
    }

    private void buildFooter() {
        this.setItem(49, BukkitUtils.deserializeItemStack(
                "ARROW : 1 : name>§cVoltar : desc>§7Para o perfil"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (!evt.getInventory().equals(this.getInventory())) return;
        evt.setCancelled(true);

        if (!evt.getWhoClicked().equals(this.player)) return;
        if (evt.getClickedInventory() == null
                || !evt.getClickedInventory().equals(this.getInventory())) return;

        Profile p = Profile.getProfile(this.player.getName());
        if (p == null) { this.player.closeInventory(); return; }

        ItemStack item = evt.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = evt.getSlot();

        if (slot == SLOT_CAT_INGAME  && category != MenuCategory.INGAME)  { reopen(MenuCategory.INGAME);  return; }
        if (slot == SLOT_CAT_CHAT    && category != MenuCategory.CHAT)    { reopen(MenuCategory.CHAT);    return; }
        if (slot == SLOT_CAT_SOCIALS && category != MenuCategory.SOCIALS) { reopen(MenuCategory.SOCIALS); return; }
        if (slot == SLOT_CAT_LOBBY   && category != MenuCategory.LOBBY)   { reopen(MenuCategory.LOBBY);   return; }
        if (slot == SLOT_CAT_GUILD   && category != MenuCategory.GUILD)   { reopen(MenuCategory.GUILD);   return; }

        switch (category) {
            case INGAME:
                if (slot == toggleSlot(0)) { cycleAndReopen("aq", AutoQueue.class,      AutoQueue.NENHUM); }
                if (slot == toggleSlot(1)) { cycleAndReopen("mr", MapRating.class,      MapRating.TODOS); }
                if (slot == toggleSlot(2)) { cycleAndReopen("bp", BloodParticles.class, BloodParticles.TODOS); }
                if (slot == toggleSlot(3)) { cycleAndReopen("ms", MapSelector.class,    MapSelector.TODOS); }
                break;

            case CHAT:
                if (slot == toggleSlot(0)) { cycleAndReopen("mn", Mentions.class,          Mentions.TODOS); }
                if (slot == toggleSlot(1)) { cycleAndReopen("ch", ChatMessages.class,      ChatMessages.TODOS); }
                if (slot == toggleSlot(2)) { cycleAndReopen("wf", WordFilter.class,        WordFilter.TODOS); }
                if (slot == toggleSlot(3)) { cycleAndReopen("lm", LobbyJoinMessages.class, LobbyJoinMessages.TODOS); }
                break;

            case SOCIALS:
                if (slot == toggleSlot(0)) { cycleAndReopen("pm", PrivateMessages.class, PrivateMessages.TODOS); }
                break;

            case LOBBY:
                if (slot == toggleSlot(0)) {
                    TimeOfDay next = getEnum("td", TimeOfDay.class, TimeOfDay.DIA).next();
                    setEnumOrdinal("td", next);
                    this.player.setPlayerTime(next.getTime(), false);
                    reopen(category);
                }
                if (slot == toggleSlot(1)) {
                    PlayerVisibility next = getEnum("pv", PlayerVisibility.class, PlayerVisibility.TODOS).next();
                    setEnumOrdinal("pv", next);
                    applyPlayerVisibility(next);
                    reopen(category);
                }
                if (slot == toggleSlot(2)) { cycleAndReopen("fly", FlyOnJoin.class, FlyOnJoin.NENHUM); }
                break;

            case GUILD:
                if (slot == toggleSlot(0)) { cycleAndReopen("gc", GuildChat.class,          GuildChat.TODOS); }
                if (slot == toggleSlot(1)) { cycleAndReopen("gn", GuildNotifications.class, GuildNotifications.TODOS); }
                break;

            default:
                break;
        }

        if (slot == 49) {
            this.cancel();
            new MenuProfile(profile);
        }
    }

    private void reopen(MenuCategory cat) {
        this.cancel();
        new MenuPreferences(profile, cat);
    }

    private <T extends Enum<T>> void cycleAndReopen(String key, Class<T> clazz, T defaultVal) {
        T current = getEnum(key, clazz, defaultVal);
        try {
            T next = (T) clazz.getMethod("next").invoke(current);
            setEnumOrdinal(key, next);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        reopen(category);
    }

    private void applyPlayerVisibility(PlayerVisibility pv) {
        if (pv == PlayerVisibility.NENHUM) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(this.player)) this.player.hidePlayer(online);
            }
            this.player.sendMessage("§cVocê está escondendo outros jogadores.");
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(this.player)) this.player.showPlayer(online);
            }
            this.player.sendMessage("§aVocê voltou a ver outros jogadores.");
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getEnum(String key, Class<T> clazz, T defaultVal) {
        JSONObject json = getPrefsJson();
        long val = json.containsKey(key) ? (long) json.get(key) : (long) defaultVal.ordinal();
        try {
            return (T) clazz.getMethod("getByOrdinal", long.class).invoke(null, val);
        } catch (Exception ex) {
            return defaultVal;
        }
    }


    private void setEnumOrdinal(String key, Enum<?> value) {
        JSONObject json = getPrefsJson();
        json.put(key, (long) value.ordinal());
        savePrefsJson(json);

        StorageImplementation storage = Main.getInstance().getStorage();
        if (storage != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                    profile.save(storage)
            );
        }
    }


    private JSONObject getPrefsJson() {
        DataContainer dc = profile.getDataContainer("VoidProfile", "preferences");
        if (dc == null) return new JSONObject();

        Object raw = dc.get();
        if (raw == null) return new JSONObject();

        String str = raw.toString().trim();

        if (!str.startsWith("{")) return new JSONObject();

        try {
            Object parsed = new org.json.simple.parser.JSONParser().parse(str);
            if (parsed instanceof JSONObject) return (JSONObject) parsed;
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[MenuPreferences] Falha ao parsear preferences de "
                    + profile.getName() + ": " + ex.getMessage());
        }
        return new JSONObject();
    }

    private void savePrefsJson(JSONObject json) {
        DataContainer dc = profile.getDataContainer("VoidProfile", "preferences");
        if (dc != null) dc.set(json.toJSONString());
    }

    public void cancel() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        if (evt.getPlayer().equals(this.player)) this.cancel();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getPlayer().equals(this.player)
                && evt.getInventory().equals(this.getInventory())) {
            this.cancel();
        }
    }
}