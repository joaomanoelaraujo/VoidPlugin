package org.ltzin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.player.Profile;
import org.ltzin.player.role.Role;
import org.ltzin.tab.TabManager;
import org.ltzin.utils.StringUtils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando /tag — lista as tags que o jogador tem permissão para usar e
 * permite selecionar uma clicando nela no chat.
 *
 * <p>A tag escolhida é salva em VoidProfile.selected_tag (via {@link Profile})
 * e persiste entre logins/restarts. Ela só deixa de valer se a permissão do
 * rank correspondente for removida (verificado em tempo real, sem precisar
 * apagar o valor salvo) ou se o jogador trocar de tag.</p>
 */
public class TagCmd extends Commands {

    public TagCmd() {
        super("tag");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return;
        }

        Player player = (Player) sender;

        if (args.length >= 2 && args[0].equalsIgnoreCase("select")) {
            handleSelect(player, args[1]);
            return;
        }

        listTags(player);
    }

    private void listTags(Player player) {
        Profile profile = Profile.getProfile(player);
        if (profile == null) {
            player.sendMessage("§cSeu perfil ainda não foi carregado, tente novamente em alguns instantes.");
            return;
        }

        List<Role> available = Role.listRoles().stream()
                .filter(Role::hasTag)
                .filter(r -> r.has(player))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            player.sendMessage("§cVocê não possui nenhuma tag disponível.");
            return;
        }

        String currentTagId = profile.getSelectedTag();

        player.sendMessage("§6Tags disponíveis:");

        TextComponent line = new TextComponent("");

        for (int i = 0; i < available.size(); i++) {
            Role role     = available.get(i);
            String rawName = StringUtils.stripColors(role.getName());
            String color    = StringUtils.getLastColor(role.getPrefix());
            boolean selected = rawName.equalsIgnoreCase(currentTagId);

            TextComponent part = new TextComponent((selected ? "§l§n" : "") + color + rawName);
            part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag select " + rawName));
            line.addExtra(part);

            if (i < available.size() - 1) {
                line.addExtra(new TextComponent("§7, "));
            } else {
                line.addExtra(new TextComponent("§7."));
            }
        }

        player.spigot().sendMessage(line);
        player.sendMessage("§7Clique em uma tag para selecioná-la.");
    }

    private void handleSelect(Player player, String roleName) {
        Role role = Role.listRoles().stream()
                .filter(r -> StringUtils.stripColors(r.getName()).equalsIgnoreCase(roleName))
                .findFirst()
                .orElse(null);

        if (role == null || !role.hasTag()) {
            player.sendMessage("§cTag inválida.");
            return;
        }

        if (!role.has(player)) {
            player.sendMessage("§cVocê não tem mais permissão para usar essa tag.");
            return;
        }

        Profile profile = Profile.getProfile(player);
        if (profile == null) {
            player.sendMessage("§cSeu perfil ainda não foi carregado, tente novamente em alguns instantes.");
            return;
        }

        String rawName = StringUtils.stripColors(role.getName());
        profile.setSelectedTag(rawName);

        TabManager.refresh(player);

        player.sendMessage("§aTag selecionada: " + role.getTagDisplay() + "§r§a!");
    }
}