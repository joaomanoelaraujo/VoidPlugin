package org.ltzin.player.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

public class VirtualTeam {

  private Score instance;

  private String name;
  private String prefix;
  private String entry;
  private String suffix;

  private int line;

  protected VirtualTeam(Score instance, String team, int line) {
    this.name = team;
    this.line = line;
    this.instance = instance;
  }

  public void destroy() {
    if (this.instance != null && this.instance.getScoreboard() != null) {
      if (this.entry != null) {
        this.instance.getScoreboard().resetScores(this.entry);
      }
      Team team = this.instance.getScoreboard().getTeam(this.name);
      if (team != null) {
        team.unregister();
      }
    }

    this.instance = null;
    this.name = null;
    this.prefix = null;
    this.entry = null;
    this.suffix = null;
    this.line = -1;
  }

  @SuppressWarnings("deprecation")
  public void update() {
    if (this.instance == null || this.instance.getScoreboard() == null || this.entry == null) {
      return;
    }

    Team team = this.instance.getScoreboard().getTeam(this.name);
    if (team == null) {
      try {
        team = this.instance.getScoreboard().registerNewTeam(this.name);
      } catch (IllegalArgumentException ignore) {
        team = this.instance.getScoreboard().getTeam(this.name);
      }
    }

    if (team == null) {
      return;
    }

    team.setPrefix(this.prefix);
    if (!team.hasEntry(this.entry)) {
      team.addEntry(this.entry);
    }
    team.setSuffix(this.suffix);

    this.instance.getObjective().getScore(this.entry).setScore(this.line);
  }

  /**
   * Divide o texto em prefix/suffix de um time, usando um "player" falso
   * (codigo de cor invisivel) como entry - funciona em qualquer versao,
   * de 1.8 ate as mais recentes, sem precisar de NMS.
   */
  public void setValue(String text) {
    text = ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);

    int max = Score.maxPrefixSuffixLength();

    // entry unico e invisivel por linha (nunca colide com nome de player real)
    this.entry = ChatColor.values()[this.line - 1].toString() + ChatColor.RESET;

    if (text.length() > max * 2) {
      text = text.substring(0, max * 2);
    }

    this.prefix = text.substring(0, Math.min(text.length(), max));
    String remaining;

    if (this.prefix.length() == max && this.prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
      this.prefix = this.prefix.substring(0, this.prefix.length() - 1);
      remaining = text.substring(this.prefix.length());
    } else {
      remaining = text.substring(Math.min(text.length(), this.prefix.length()));
    }

    this.suffix = ChatColor.getLastColors(this.prefix) + remaining;
    this.suffix = this.suffix.substring(0, Math.min(max, this.suffix.length()));
    if (this.suffix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
      this.suffix = this.suffix.substring(0, this.suffix.length() - 1);
    }
  }
}