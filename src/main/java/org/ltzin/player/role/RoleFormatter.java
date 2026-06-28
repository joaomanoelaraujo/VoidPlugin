package org.ltzin.player.role;

import org.ltzin.utils.StringUtils;
import org.bukkit.entity.Player;

/**
 * Formata nomes de jogadores com prefixo de rank ou cor de rank.
 *
 * <p>Não faz lookup — delega ao {@link RoleLookup} internamente.</p>
 *
 * <h3>Exemplos</h3>
 * <pre>{@code
 *   // Prefixo completo:      "[VIP] Joao"
 *   RoleFormatter.withPrefix("Joao");
 *
 *   // Só a cor do rank:      "§6Joao"
 *   RoleFormatter.withColor("Joao");
 *
 *   // Sobrepor outra cor no prefixo:
 *   RoleFormatter.withPrefix("Joao", "&6");
 *
 *   // A partir de um Player online:
 *   RoleFormatter.withPrefix(player);
 * }</pre>
 */
public final class RoleFormatter {

    private RoleFormatter() {}


    /** Retorna {@code "PREFIXO + nome"}. */
    public static String withPrefix(String playerName) {
        return withPrefix(playerName, null);
    }

    /**
     * Retorna {@code "PREFIXO + nome"} com uma cor de destaque sobreposta
     * à primeira cor do prefixo, mantendo formatações adicionais (negrito, etc.).
     *
     * @param colorCode código de cor Minecraft (ex.: {@code "&6"} ou {@code "§6"})
     */
    public static String withPrefix(String playerName, String colorCode) {
        RoleLookup.Result result = RoleLookup.resolve(playerName);
        String prefix = applyColorOverride(result.getRole().getPrefix(), colorCode);
        return prefix + result.getResolvedName();
    }

    /**
     * Retorna o nome colorido com a cor do rank (sem o texto do prefixo).
     * Ex.: {@code "§6Joao"} para um VIP dourado.
     */
    public static String withColor(String playerName) {
        RoleLookup.Result result = RoleLookup.resolve(playerName);
        String color = StringUtils.getLastColor(result.getRole().getPrefix());
        return color + result.getResolvedName();
    }


    /** Retorna {@code "PREFIXO + nome"} para um jogador online. */
    public static String withPrefix(Player player) {
        return withPrefix(player, null);
    }

    /** Retorna {@code "PREFIXO + nome"} para um jogador online com cor sobreposta. */
    public static String withPrefix(Player player, String colorCode) {
        RoleLookup.Result result = RoleLookup.resolveOnline(player);
        String prefix = applyColorOverride(result.getRole().getPrefix(), colorCode);
        return prefix + result.getResolvedName();
    }

    /** Retorna o nome colorido com a cor do rank para um jogador online. */
    public static String withColor(Player player) {
        RoleLookup.Result result = RoleLookup.resolveOnline(player);
        String color = StringUtils.getLastColor(result.getRole().getPrefix());
        return color + result.getResolvedName();
    }


    /**
     * Substitui a primeira cor do prefixo por {@code colorCode}, mantendo
     * o restante do texto. Se {@code colorCode} for null ou vazio, retorna
     * o prefixo inalterado.
     */
    private static String applyColorOverride(String prefix, String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) return prefix;
        return colorCode + prefix.replaceFirst("§[0-9a-fk-orA-FK-OR]", "");
    }
}