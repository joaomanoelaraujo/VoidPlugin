package org.ltzin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.ltzin.Main;
import org.ltzin.database.data.PreferencesContainer;
import org.ltzin.player.Profile;
import org.ltzin.player.enums.ChatMessages;
import org.ltzin.player.enums.LobbyJoinMessages;
import org.ltzin.player.enums.Mentions;
import org.ltzin.player.enums.PrivateMessages;
import org.ltzin.player.enums.WordFilter;
import org.ltzin.player.preferences.PlayerPreference;
import org.ltzin.player.role.Role;
import org.ltzin.player.role.RoleLookup;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PreferencesListener implements Listener {

    private static final String[] SWEAR_WORDS = {
            "merda", "caralho", "viado"
    };

    private static final String MENTION_COLOR = "§b";

    public static void setup() {
        Main.getInstance().getServer().getPluginManager()
                .registerEvents(new PreferencesListener(), Main.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent evt) {
        Player sender          = evt.getPlayer();
        String originalMessage = evt.getMessage();
        String format          = evt.getFormat();

        boolean hasSwear = containsSwear(originalMessage);
        String  censored = hasSwear ? censorSwears(originalMessage) : originalMessage;

        Profile senderProfile      = Profile.getProfile(sender.getName());
        boolean senderFilterActive = senderProfile != null
                && senderProfile.getPreferences().get(PlayerPreference.WORD_FILTER) == WordFilter.TODOS;

        Set<Player> mentioned = findMentioned(originalMessage);

        // Respeita o que listeners de prioridade mais alta ja decidiram (ex: o
        // vSkyWars, que restringe os destinatarios pra quem esta na mesma
        // sala/mundo). Se nenhum outro listener mexeu nisso, o padrao do Bukkit
        // ja e "todo mundo online", entao o comportamento fora de partida
        // continua igual.
        Set<Player> candidates = new HashSet<>(evt.getRecipients());
        evt.getRecipients().clear();

        for (Player online : candidates) {
            if (online.equals(sender)) continue;

            Profile profile = Profile.getProfile(online.getName());
            if (profile == null) continue;

            PreferencesContainer prefs = profile.getPreferences();

            if (prefs.get(PlayerPreference.CHAT_MESSAGES) == ChatMessages.NENHUM) continue;

            boolean isMentioned    = mentioned.contains(online);
            boolean mentionsActive = prefs.get(PlayerPreference.MENTIONS) == Mentions.TODOS;

            String base = (hasSwear && prefs.get(PlayerPreference.WORD_FILTER) == WordFilter.TODOS)
                    ? censored : originalMessage;

            String messageToSend = (isMentioned && mentionsActive)
                    ? highlightMention(base, online) : base;

            online.sendMessage(String.format(format, sender.getDisplayName(), messageToSend));

            if (isMentioned && mentionsActive) {
                online.playSound(online.getLocation(), Sound.LEVEL_UP, 0.5f, 2.0f);
            }
        }

        String senderMessage = (hasSwear && senderFilterActive) ? censored : originalMessage;
        sender.sendMessage(String.format(format, sender.getDisplayName(), senderMessage));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player joiningPlayer = evt.getPlayer();

        Role joiningRole = RoleLookup.roleForOnlinePlayer(joiningPlayer);
        if (joiningRole == null || !joiningRole.isBroadcast()) return;

        String tag     = Role.getTag(joiningPlayer);
        String message = tag + " §fentrou no servidor!";

        for (Player online : Bukkit.getOnlinePlayers()) {
            Profile onlineProfile = Profile.getProfile(online.getName());

            if (onlineProfile == null) continue;

            if (onlineProfile.playingGame()) continue;

            if (onlineProfile.getPreferences().get(PlayerPreference.LOBBY_JOIN_MESSAGES) == LobbyJoinMessages.TODOS) {
                online.sendMessage(message);
            }
        }
    }


    private Set<Player> findMentioned(String message) {
        Set<Player> result = new HashSet<>();
        String lower = message.toLowerCase();

        for (Player online : Bukkit.getOnlinePlayers()) {
            String nick = online.getName().toLowerCase();
            if (lower.contains("@" + nick)
                    || Pattern.compile("(?<![a-zA-Z0-9_@])" + Pattern.quote(nick) + "(?![a-zA-Z0-9_])")
                    .matcher(lower).find()) {
                result.add(online);
            }
        }
        return result;
    }


    private String highlightMention(String message, Player mentioned) {
        String nick = mentioned.getName();

        message = message.replaceAll(
                "(?i)" + Pattern.quote("@" + nick),
                MENTION_COLOR + "@" + nick + "§r");

        message = message.replaceAll(
                "(?i)(?<![a-zA-Z0-9_@])" + Pattern.quote(nick) + "(?![a-zA-Z0-9_])",
                MENTION_COLOR + nick + "§r");

        return message;
    }

    private boolean containsSwear(String message) {
        String lower = message.toLowerCase();
        for (String word : SWEAR_WORDS) {
            if (lower.contains(word)) return true;
        }
        return false;
    }

    private String censorSwears(String message) {
        for (String word : SWEAR_WORDS) {
            StringBuilder asterisks = new StringBuilder();
            for (int i = 0; i < word.length(); i++) asterisks.append('*');
            message = message.replaceAll("(?i)" + Pattern.quote(word), asterisks.toString());
        }
        return message;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrivateMessage(PlayerCommandPreprocessEvent evt) {
        String raw = evt.getMessage().toLowerCase();

        if (!raw.startsWith("/msg ")
                && !raw.startsWith("/tell ")
                && !raw.startsWith("/w ")
                && !raw.startsWith("/r ")
                && !raw.startsWith("/reply ")) return;

        String[] parts = evt.getMessage().split(" ", 3);
        if (parts.length < 2) return;

        if (raw.startsWith("/r ") || raw.startsWith("/reply ")) return;

        String targetName = parts[1];
        Player target     = Bukkit.getPlayerExact(targetName);
        if (target == null) return;

        Profile targetProfile = Profile.getProfile(target.getName());
        if (targetProfile == null) return;

        PrivateMessages pref = targetProfile.getPreferences().get(PlayerPreference.PRIVATE_MESSAGES);

        if (pref == PrivateMessages.NENHUM) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage("§cEste jogador está com mensagens privadas desativadas.");
        }
    }
}