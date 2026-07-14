package org.ltzin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.ltzin.reload.PluginReloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class VReloadCmd extends Commands {

  private static final long CONFIRM_WINDOW_MS = 10_000L;
  private static final String PERMISSION = "voidless.reload";

  private final Map<String, List<String>> groups = new LinkedHashMap<>();
  private final Map<String, Long> pendingConfirmation = new HashMap<>();

  public VReloadCmd() {
    super("vreload");
    this.setPermission(PERMISSION);

    this.groups.put("vskywars", Collections.singletonList("vSkyWars"));
    this.groups.put("voidlessplugin", Arrays.asList("VoidlessPlugin", "vSkyWars"));
  }

  @Override
  public void perform(CommandSender sender, String label, String[] args) {
    if (!sender.hasPermission(PERMISSION)) {
      sender.sendMessage(ChatColor.RED + "Voce nao tem permissao para usar esse comando.");
      return;
    }

    if (args.length < 1) {
      sender.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " <grupo> [--force]");
      sender.sendMessage(ChatColor.GRAY + "Grupos disponiveis: " + String.join(", ", this.groups.keySet()));
      return;
    }

    String groupKey = args[0].toLowerCase();
    boolean force = args.length > 1 && args[1].equalsIgnoreCase("--force");

    List<String> chain = this.groups.get(groupKey);
    if (chain == null) {
      sender.sendMessage(ChatColor.RED + "'" + args[0] + "' nao e um grupo valido.");
      sender.sendMessage(ChatColor.GRAY + "Grupos disponiveis: " + String.join(", ", this.groups.keySet()));
      return;
    }

    int online = Bukkit.getOnlinePlayers().size();
    if (online > 0 && !force) {
      sender.sendMessage(ChatColor.RED + "Ha " + online + " jogador(es) online. Reload de plugin em producao com "
              + "jogadores conectados NAO e recomendado (risco de bug intermitente por classloader).");
      sender.sendMessage(ChatColor.RED + "Se voce entende o risco e quer continuar mesmo assim, use: /" + label + " " + groupKey + " --force");
      return;
    }

    String confirmKey = sender.getName() + ":" + groupKey;
    long now = System.currentTimeMillis();
    Long pending = this.pendingConfirmation.get(confirmKey);

    if (pending == null || now - pending > CONFIRM_WINDOW_MS) {
      this.pendingConfirmation.put(confirmKey, now);
      sender.sendMessage(ChatColor.GOLD + "Isso vai recarregar: " + ChatColor.WHITE + String.join(" -> ", chain));
      sender.sendMessage(ChatColor.GOLD + "Tem certeza? Digite o comando novamente nos proximos 10 segundos para confirmar.");
      return;
    }

    this.pendingConfirmation.remove(confirmKey);

    sender.sendMessage(ChatColor.YELLOW + "Recarregando: " + String.join(" -> ", chain) + "...");
    long start = System.currentTimeMillis();

    List<PluginReloader.Result> results = PluginReloader.reloadChain(chain);

    long elapsed = System.currentTimeMillis() - start;
    boolean allSuccess = true;

    for (int i = 0; i < chain.size(); i++) {
      PluginReloader.Result result = results.get(i);
      if (result.isSuccess()) {
        sender.sendMessage(ChatColor.GREEN + "OK " + result.message());
      } else {
        allSuccess = false;
        sender.sendMessage(ChatColor.RED + "FALHA " + result.message());
      }
    }

    sender.sendMessage((allSuccess ? ChatColor.GREEN : ChatColor.RED) + "Concluido em " + elapsed + "ms.");
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
    if (args.length == 1) {
      String partial = args[0].toLowerCase();
      List<String> out = new ArrayList<>();
      for (String key : this.groups.keySet()) {
        if (key.startsWith(partial)) {
          out.add(key);
        }
      }
      return out;
    }

    if (args.length == 2 && this.groups.containsKey(args[0].toLowerCase())) {
      return Collections.singletonList("--force");
    }

    return Collections.emptyList();
  }
}