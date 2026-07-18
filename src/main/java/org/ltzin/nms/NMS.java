package org.ltzin.nms;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import org.bukkit.entity.Player;
import org.ltzin.Main;

public final class NMS {
  private static final ProtocolManager PROTOCOL = ProtocolLibrary.getProtocolManager();

  private static final MinecraftVersion V1_12 = new MinecraftVersion("1.12.0");
  private static final MinecraftVersion V1_17 = new MinecraftVersion("1.17.0");
  private static final MinecraftVersion CURRENT_VERSION = MinecraftVersion.getCurrentVersion();

  private NMS() {
  }

  public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    sendTimes(player, fadeIn, stay, fadeOut);
    sendAction(player, TitleAction.SUBTITLE, subtitle);
    sendAction(player, TitleAction.TITLE, title);
  }

  private static void sendTimes(Player player, int fadeIn, int stay, int fadeOut) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.TITLE);
      packet.getTitleActions().write(0, TitleAction.TIMES);
      packet.getIntegers().write(0, fadeIn).write(1, stay).write(2, fadeOut);
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      warn(player, "TIMES", e);
    }
  }

  private static void sendAction(Player player, EnumWrappers.TitleAction action, String text) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.TITLE);
      packet.getTitleActions().write(0, action);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(text == null ? "" : text));
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      warn(player, action.name(), e);
    }

  }

  private static void warn(Player player, String stage, Exception e) {
    Main.getInstance().getMyLogger().warning("[NMS] Falha ao enviar TITLE (" + stage + ") para " + player.getName() + ": " + e.getMessage());
  }

  public static void sendTabHeaderFooter(Player player, String header, String footer) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.PLAYER_LIST_HEADER_FOOTER);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(header == null ? "" : header));
      packet.getChatComponents().write(1, WrappedChatComponent.fromText(footer == null ? "" : footer));
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      Main.getInstance().getMyLogger().warning("[NMS] Falha ao enviar header/footer do tablist para " + player.getName() + ": " + e.getMessage());
    }

  }

  public static void sendActionBar(Player player, String text) {
    String safe = text == null ? "" : text;

    if (CURRENT_VERSION.compareTo(V1_17) >= 0) {
      if (trySendActionBarModern(player, safe)) return;
      if (trySendActionBarChatType(player, safe)) return;
      trySendActionBarLegacyByte(player, safe);
      return;
    }

    if (CURRENT_VERSION.compareTo(V1_12) >= 0) {
      if (trySendActionBarChatType(player, safe)) return;
      if (trySendActionBarModern(player, safe)) return;
      trySendActionBarLegacyByte(player, safe);
      return;
    }

    if (trySendActionBarLegacyByte(player, safe)) return;
    if (trySendActionBarChatType(player, safe)) return;
    trySendActionBarModern(player, safe);
  }

  private static boolean trySendActionBarModern(Player player, String text) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.SET_ACTION_BAR_TEXT);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
      PROTOCOL.sendServerPacket(player, packet);
      return true;
    } catch (Exception e) {
      warnActionBar(player, "SET_ACTION_BAR_TEXT", e);
      return false;
    }
  }

  private static boolean trySendActionBarChatType(Player player, String text) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.CHAT);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
      packet.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
      PROTOCOL.sendServerPacket(player, packet);
      return true;
    } catch (Exception e) {
      warnActionBar(player, "CHAT/ChatType", e);
      return false;
    }
  }

  private static boolean trySendActionBarLegacyByte(Player player, String text) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(Server.CHAT);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
      packet.getBytes().write(0, (byte) 2);
      PROTOCOL.sendServerPacket(player, packet);
      return true;
    } catch (Exception e) {
      warnActionBar(player, "CHAT/byte", e);
      return false;
    }
  }

  private static void warnActionBar(Player player, String stage, Exception e) {
    Main.getInstance().getMyLogger().warning("[NMS] Falha ao enviar ACTIONBAR (" + stage + ") para " + player.getName() + ": " + e.getMessage());
  }
}