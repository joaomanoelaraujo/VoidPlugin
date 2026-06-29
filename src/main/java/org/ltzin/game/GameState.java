package org.ltzin.game;

public enum GameState {
  WAITING, STARTING, INGAME, GAMEOVER;

  public boolean canJoin() {
    return this == WAITING;
  }
}