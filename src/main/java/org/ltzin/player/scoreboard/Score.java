package org.ltzin.player.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Scoreboard por-jogador, compativel de Spigot/PaperSpigot 1.8.8 ate as
 * versoes mais recentes, sem depender de NMS.
 *
 * A API de alto nivel do Bukkit (Scoreboard/Objective/Team) ja e mantida
 * retrocompativel pela Spigot/Paper; a unica coisa que muda entre versoes
 * e o limite de tamanho de prefix/suffix (16 chars ate a 1.12, bem maior
 * a partir da 1.13), o que essa classe detecta sozinha via reflection.
 */
@SuppressWarnings("deprecation")
public abstract class Score {

  private static final int MAX_LINES = 15;
  private static final boolean MODERN_TEAM_API;

  static {
    boolean modern;
    try {
      Team.class.getMethod("setColor", ChatColor.class); // metodo so existe a partir da 1.13
      modern = true;
    } catch (NoSuchMethodException e) {
      modern = false;
    }
    MODERN_TEAM_API = modern;
  }

  static int maxPrefixSuffixLength() {
    return MODERN_TEAM_API ? 64 : 16;
  }

  private Player player;
  private Objective objective;
  private Scoreboard scoreboard;
  private ScoreboardScroller scroller;

  private String display;
  private boolean health, healthTab;

  private VirtualTeam[] teams = new VirtualTeam[MAX_LINES];

  public Score() {}

  public void scroll() {
    if (this.scroller != null) {
      display(this.scroller.next());
    }
  }

  public void update() {}

  public void updateHealth() {
    if (this.healthTab && this.scoreboard != null) {
      Objective tabObjective = this.scoreboard.getObjective("healthPL");
      if (tabObjective != null) {
        for (Player online : Bukkit.getOnlinePlayers()) {
          tabObjective.getScore(online.getName()).setScore((int) online.getHealth());
        }
      }
    }
  }

  public Score add(int line) {
    return add(line, "");
  }

  public Score add(int line, String name) {
    if (line > MAX_LINES || line < 1 || this.teams == null) {
      return this;
    }

    VirtualTeam team = getOrCreate(line);
    team.setValue(name);
    if (this.scoreboard != null) {
      team.update();
    }
    return this;
  }

  public Score remove(int line) {
    if (line > MAX_LINES || line < 1 || this.teams == null) {
      return this;
    }

    VirtualTeam team = this.teams[line - 1];
    if (team != null) {
      team.destroy();
      this.teams[line - 1] = null;
    }
    return this;
  }

  public Score to(Player player) {
    Player lastPlayer = this.player;
    this.player = player;
    if (this.scoreboard != null) {
      if (lastPlayer != null) {
        lastPlayer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }
      player.setScoreboard(this.scoreboard);
    }
    return this;
  }

  public Score display(String display) {
    this.display = ChatColor.translateAlternateColorCodes('&', display == null ? "" : display);
    if (this.objective != null) {
      this.objective.setDisplayName(this.display.substring(0, Math.min(this.display.length(), 32)));
    }
    return this;
  }

  public Score scroller(ScoreboardScroller ss) {
    this.scroller = ss;
    return this;
  }

  public Score health() {
    this.health = !this.health;
    if (this.scoreboard != null) {
      if (!this.health) {
        Objective obj = this.scoreboard.getObjective("healthBN");
        if (obj != null) obj.unregister();
      } else {
        Objective healthObj = this.scoreboard.registerNewObjective("healthBN", "health");
        healthObj.setDisplayName("§c❤");
        healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
      }
    }
    return this;
  }

  public Score healthTab() {
    this.healthTab = !this.healthTab;
    if (this.scoreboard != null) {
      if (!this.healthTab) {
        Objective obj = this.scoreboard.getObjective("healthPL");
        if (obj != null) obj.unregister();
      } else {
        Objective tab = this.scoreboard.registerNewObjective("healthPL", "dummy");
        tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
      }
    }
    return this;
  }

  public Score build() {
    this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    this.objective = this.scoreboard.registerNewObjective(getObjectiveName(), "dummy");
    this.objective.setDisplayName(this.display == null ? "" : this.display.substring(0, Math.min(this.display.length(), 32)));
    this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

    if (this.player != null) {
      this.player.setScoreboard(this.scoreboard);
    }

    if (this.health) {
      Objective healthObj = this.scoreboard.registerNewObjective("healthBN", "health");
      healthObj.setDisplayName("§c❤");
      healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    if (this.healthTab) {
      Objective tab = this.scoreboard.registerNewObjective("healthPL", "dummy");
      tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
      for (Player online : Bukkit.getOnlinePlayers()) {
        tab.getScore(online.getName()).setScore((int) online.getHealth());
      }
    }

    for (VirtualTeam team : this.teams) {
      if (team != null) {
        team.update();
      }
    }

    return this;
  }

  public void destroy() {
    if (this.objective != null) {
      this.objective.unregister();
    }
    if (this.scoreboard != null) {
      if (this.health) {
        Objective obj = this.scoreboard.getObjective("healthBN");
        if (obj != null) obj.unregister();
      }
      if (this.healthTab) {
        Objective obj = this.scoreboard.getObjective("healthPL");
        if (obj != null) obj.unregister();
      }
    }

    if (this.player != null) {
      this.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    this.objective = null;
    this.scoreboard = null;
    this.teams = null;
    this.player = null;
    this.display = null;
  }

  public VirtualTeam getTeam(int line) {
    if (line > MAX_LINES || line < 1 || this.teams == null) {
      return null;
    }
    return this.teams[line - 1];
  }

  public VirtualTeam getOrCreate(int line) {
    if (line > MAX_LINES || line < 1) {
      return null;
    }
    if (this.teams[line - 1] == null) {
      this.teams[line - 1] = new VirtualTeam(this, "kbLine" + line, line);
    }
    return this.teams[line - 1];
  }

  public String getObjectiveName() {
    return "kScoreboard";
  }

  public Scoreboard getScoreboard() {
    return this.scoreboard;
  }

  public Objective getObjective() {
    return this.objective;
  }
}