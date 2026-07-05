package org.ltzin.player.scoreboard;

import java.util.List;

public class ScoreboardScroller {

  private int index;
  private final List<String> frames;

  public ScoreboardScroller(List<String> frames) {
    this.index = -1;
    this.frames = frames;
  }

  public String next() {
    if (this.frames == null || this.frames.isEmpty()) {
      return "";
    }
    if (++this.index >= this.frames.size()) {
      this.index = 0;
    }
    return this.frames.get(this.index);
  }
}