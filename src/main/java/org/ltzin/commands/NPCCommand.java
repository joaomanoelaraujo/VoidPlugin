package org.ltzin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ltzin.npc.NPC;
import org.ltzin.npc.NPCManager;
import org.ltzin.utils.SkinUtil;

public class NPCCommand extends Commands {

    private final JavaPlugin plugin;
    private final NPCManager manager;

    public NPCCommand(JavaPlugin plugin, NPCManager manager) {
        super("npc");
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /npc <create|remove|skin|command|moveto|list>");
            return;
        }

        switch (args[0].toLowerCase()) {

            case "create": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /npc create <id>");
                    return;
                }

                String id = args[1];

                if (manager.exists(id)) {
                    sender.sendMessage(ChatColor.RED + "Já existe um NPC com esse id.");
                    return;
                }

                manager.create(id, player.getLocation());
                manager.save();

                sender.sendMessage(ChatColor.GREEN + "NPC '" + id + "' criado.");
                sender.sendMessage(ChatColor.YELLOW + "Use /npc skin " + id + " <jogador> para aplicar uma skin.");
                break;
            }

            case "remove": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /npc remove <id>");
                    return;
                }

                if (!manager.exists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "NPC não encontrado.");
                    return;
                }

                manager.remove(args[1]);
                manager.save();

                sender.sendMessage(ChatColor.GREEN + "NPC removido.");
                break;
            }

            case "skin": {

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /npc skin <id> <nome>");
                    return;
                }

                NPC npc = manager.get(args[1]);

                if (npc == null) {
                    sender.sendMessage(ChatColor.RED + "NPC não encontrado.");
                    return;
                }

                String targetName = args[2];

                sender.sendMessage(ChatColor.YELLOW + "Buscando skin de " + targetName + "...");

                SkinUtil.fetchSkin(targetName).thenAccept(data ->
                        Bukkit.getScheduler().runTask(plugin, () -> {

                            if (data == null) {
                                sender.sendMessage(ChatColor.RED + "Não foi possível encontrar essa skin.");
                                return;
                            }

                            npc.setSkin(targetName, data.value, data.signature);

                            manager.remove(npc.getId());

                            NPC recreated = manager.create(npc.getId(), npc.getLocation());
                            recreated.setSkin(targetName, data.value, data.signature);
                            recreated.setCommand(npc.getCommand());

                            for (Player online : Bukkit.getOnlinePlayers()) {
                                manager.spawnFor(recreated, online);
                            }

                            manager.save();

                            sender.sendMessage(ChatColor.GREEN + "Skin aplicada ao NPC '" + npc.getId() + "'.");
                        })
                );

                break;
            }

            case "command": {

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /npc command <id> <comando>");
                    return;
                }

                NPC npc = manager.get(args[1]);

                if (npc == null) {
                    sender.sendMessage(ChatColor.RED + "NPC não encontrado.");
                    return;
                }

                npc.setCommand(joinFrom(args, 2));

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Comando definido.");
                break;
            }

            case "moveto": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /npc moveto <id>");
                    return;
                }

                NPC npc = manager.get(args[1]);

                if (npc == null) {
                    sender.sendMessage(ChatColor.RED + "NPC não encontrado.");
                    return;
                }

                manager.teleport(npc, player.getLocation());

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "NPC movido.");
                break;
            }

            case "list": {

                if (manager.getAll().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Nenhum NPC criado.");
                    return;
                }

                sender.sendMessage(ChatColor.YELLOW + "NPCs:");

                manager.getAll().forEach(npc ->
                        sender.sendMessage(ChatColor.GRAY + "- " + npc.getId()));

                break;
            }

            default:
                sender.sendMessage(ChatColor.RED + "Subcomando desconhecido.");
                break;
        }
    }

    private String joinFrom(String[] args, int start) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }

        return builder.toString();
    }
}