package org.ltzin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.hologram.Hologram;
import org.ltzin.hologram.HologramManager;

public class HoloCommand extends Commands {

    private final HologramManager manager;

    public HoloCommand(HologramManager manager) {
        super("holo");
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
            sender.sendMessage(ChatColor.YELLOW + "Uso: /holo <create|addline|setline|removeline|remove|list|movehere>");
            return;
        }

        switch (args[0].toLowerCase()) {

            case "create": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo create <id> <texto...>");
                    return;
                }

                String id = args[1];

                if (manager.exists(id)) {
                    sender.sendMessage(ChatColor.RED + "Já existe um holograma com esse id.");
                    return;
                }

                String text = args.length > 2 ? joinFrom(args, 2) : "&fNovo Holograma";

                Hologram hologram = manager.create(id, player.getLocation());
                hologram.addLine(ChatColor.translateAlternateColorCodes('&', text));

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Holograma '" + id + "' criado.");
                break;
            }

            case "addline": {

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo addline <id> <texto...>");
                    return;
                }

                Hologram hologram = manager.get(args[1]);

                if (hologram == null) {
                    sender.sendMessage(ChatColor.RED + "Holograma não encontrado.");
                    return;
                }

                hologram.addLine(ChatColor.translateAlternateColorCodes('&', joinFrom(args, 2)));

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Linha adicionada.");
                break;
            }

            case "setline": {

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo setline <id> <linha> <texto...>");
                    return;
                }

                Hologram hologram = manager.get(args[1]);

                if (hologram == null) {
                    sender.sendMessage(ChatColor.RED + "Holograma não encontrado.");
                    return;
                }

                int index;

                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Número inválido.");
                    return;
                }

                hologram.setLine(index, ChatColor.translateAlternateColorCodes('&', joinFrom(args, 3)));

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Linha alterada.");
                break;
            }

            case "removeline": {

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo removeline <id> <linha>");
                    return;
                }

                Hologram hologram = manager.get(args[1]);

                if (hologram == null) {
                    sender.sendMessage(ChatColor.RED + "Holograma não encontrado.");
                    return;
                }

                int index;

                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Número inválido.");
                    return;
                }

                hologram.removeLine(index);

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Linha removida.");
                break;
            }

            case "remove": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo remove <id>");
                    return;
                }

                if (!manager.exists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "Holograma não encontrado.");
                    return;
                }

                manager.remove(args[1]);

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Holograma removido.");
                break;
            }

            case "movehere": {

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /holo movehere <id>");
                    return;
                }

                Hologram hologram = manager.get(args[1]);

                if (hologram == null) {
                    sender.sendMessage(ChatColor.RED + "Holograma não encontrado.");
                    return;
                }

                hologram.moveTo(player.getLocation());

                manager.save();

                sender.sendMessage(ChatColor.GREEN + "Holograma movido.");
                break;
            }

            case "list": {

                if (manager.getAll().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Nenhum holograma criado.");
                    return;
                }

                sender.sendMessage(ChatColor.YELLOW + "Hologramas:");
                manager.getAll().keySet().forEach(id -> sender.sendMessage(ChatColor.GRAY + "- " + id));
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
                builder.append(' ');
            }
            builder.append(args[i]);
        }

        return builder.toString();
    }
}