package org.ltzin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.Main;
import org.ltzin.listeners.VoidlessListeners;
import org.ltzin.player.Profile;
import org.ltzin.skin.SkinData;


public class SkinCmd extends Commands {

    public SkinCmd() {
        super("skin");
    }

    @Override
    public void perform(final CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {

            case "reload": {
                if (!sender.hasPermission("voidless.skin.reload")) {
                    sender.sendMessage("§cSem permissão.");
                    return;
                }

                String nick = resolveNick(sender, args, 1);
                if (nick == null) return;

                final Player target = Bukkit.getPlayerExact(nick);
                if (target == null) {
                    sender.sendMessage("§cPlayer offline: §f" + nick);
                    return;
                }

                VoidlessListeners.getSkinFetcher().invalidate(nick);

                final String finalNick = nick;

                Main.getInstance().getServer().getScheduler()
                        .runTaskAsynchronously(Main.getInstance(), () -> {

                            final SkinData skin = VoidlessListeners.getSkinFetcher().fetch(finalNick);

                            if (skin == null) {
                                sender.sendMessage("§cSkin não encontrada para §f" + finalNick + "§c. (nick premium?)");
                                return;
                            }

                            Main.getInstance().getServer().getScheduler()
                                    .runTask(Main.getInstance(), () -> {
                                        if (!target.isOnline()) {
                                            sender.sendMessage("§c" + finalNick + " saiu durante o reload.");
                                            return;
                                        }
                                        VoidlessListeners.getSkinApplier().apply(target, skin);
                                        sender.sendMessage("§aSkin de §f" + finalNick + " §areaplicada com sucesso!");
                                    });
                        });
                break;
            }

            case "info": {
                if (!sender.hasPermission("voidless.skin.info")) {
                    sender.sendMessage("§cSem permissão.");
                    return;
                }

                String nick = resolveNick(sender, args, 1);
                if (nick == null) return;

                if (VoidlessListeners.getSkinFetcher().isCached(nick)) {
                    long age = VoidlessListeners.getSkinFetcher().cacheAgeSeconds(nick);
                    sender.sendMessage("§aSkin em cache para §f" + nick + " §a(há " + age + "s).");
                } else {
                    sender.sendMessage("§eSem skin em cache para §f" + nick + "§e.");
                }

                Profile profile = Profile.getProfile(nick);
                if (profile != null) {
                    sender.sendMessage("§7Perfil carregado: §asim");
                    sender.sendMessage("§7Skin pendente:    §" + (profile.hasPendingSkin() ? "asim" : "cnão"));
                } else {
                    sender.sendMessage("§7Perfil carregado: §cnão");
                }
                break;
            }

            default:
                sendHelp(sender);
                break;
        }
    }


    private String resolveNick(CommandSender sender, String[] args, int idx) {
        if (args.length > idx) {
            return args[idx];
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            return p.getName();
        }
        sender.sendMessage("§cEspecifique um nickname.");
        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6[Skin] §eComandos disponíveis:");
        sender.sendMessage("§e/skin reload [nick] §7- Reaplica a skin do player");
        sender.sendMessage("§e/skin info   [nick] §7- Info do cache de skin");
    }
}