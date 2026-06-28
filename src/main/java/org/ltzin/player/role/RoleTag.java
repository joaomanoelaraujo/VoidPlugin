package org.ltzin.player.role;

import org.ltzin.database.data.DataContainer;
import org.ltzin.player.Profile;

import java.util.Optional;

public final class RoleTag {

    private RoleTag() {}

    /**
     * Retorna o {@link Role} correspondente à tag customizada do jogador,
     * ou {@link Optional#empty()} se ele não tiver tag configurada ou
     * se o profile não estiver carregado.
     *
     * <p>A tag é lida da coluna {@code "tag"} da tabela {@code "VoidProfile"}.</p>
     *
     * @param playerName nome do jogador online
     */

    public static Optional<Role> getTagRole(String playerName) {
        Profile profile = Profile.getProfile(playerName);
        if (profile == null) return Optional.empty();

        DataContainer dc = profile.getDataContainer("VoidProfile", "role");
        if (dc == null) return Optional.empty();

        String tagValue = dc.getAsString();
        if (tagValue == null || tagValue.isEmpty()) return Optional.empty();

        return Optional.ofNullable(Role.byName(tagValue));
    }
}