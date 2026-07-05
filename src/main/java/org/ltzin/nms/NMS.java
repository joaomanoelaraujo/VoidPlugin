package org.ltzin.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;
import org.ltzin.Main;

/**
 * Envio de title/subtitle e header/footer do tablist, compativel de
 * Spigot/PaperSpigot 1.8.8 ate as versoes mais recentes.
 *
 * Title/subtitle usa ProtocolLib (o pacote TITLE e normalizado pela lib
 * entre as versoes de NMS). Header/footer usa a API nativa do Spigot
 * (Player#setPlayerListHeaderFooter), disponivel desde a 1.8, sem precisar
 * de pacote manual.
 */
public final class NMS {

  private NMS() {}

  private static final ProtocolManager PROTOCOL = ProtocolLibrary.getProtocolManager();

  /**
   * Envia um titulo/subtitulo para o player.
   * fadeIn/stay/fadeOut sao em ticks (20 ticks = 1 segundo).
   *
   * Ex: NMS.sendTitle(player, "", "", 0, 1, 0) -> limpa/reseta o titulo
   * atual do player quase instantaneamente (1 tick de exibicao).
   */
  public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    sendTimes(player, fadeIn, stay, fadeOut);
    sendAction(player, EnumWrappers.TitleAction.SUBTITLE, subtitle);
    sendAction(player, EnumWrappers.TitleAction.TITLE, title);
  }

  private static void sendTimes(Player player, int fadeIn, int stay, int fadeOut) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(PacketType.Play.Server.TITLE);
      packet.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);
      packet.getIntegers().write(0, fadeIn).write(1, stay).write(2, fadeOut);
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      warn(player, "TIMES", e);
    }
  }

  private static void sendAction(Player player, EnumWrappers.TitleAction action, String text) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(PacketType.Play.Server.TITLE);
      packet.getTitleActions().write(0, action);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(text == null ? "" : text));
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      warn(player, action.name(), e);
    }
  }

  private static void warn(Player player, String stage, Exception e) {
    Main.getInstance().getMyLogger().warning(
            "[NMS] Falha ao enviar TITLE (" + stage + ") para " + player.getName() + ": " + e.getMessage());
  }

  /**
   * Envia header/footer do tablist via ProtocolLib (pacote
   * PLAYER_LIST_HEADER_FOOTER), compativel desde a 1.8 - a API nativa do
   * Spigot (Player#setPlayerListHeaderFooter) so foi adicionada bem depois
   * da 1.8.8, entao nao da pra compilar contra ela aqui.
   */
  public static void sendTabHeaderFooter(Player player, String header, String footer) {
    try {
      PacketContainer packet = PROTOCOL.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(header == null ? "" : header));
      packet.getChatComponents().write(1, WrappedChatComponent.fromText(footer == null ? "" : footer));
      PROTOCOL.sendServerPacket(player, packet);
    } catch (Exception e) {
      Main.getInstance().getMyLogger().warning(
              "[NMS] Falha ao enviar header/footer do tablist para " + player.getName() + ": " + e.getMessage());
    }
  }
}