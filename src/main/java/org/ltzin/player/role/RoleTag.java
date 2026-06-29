package org.ltzin.player.role;

import org.ltzin.database.data.DataContainer;
import org.ltzin.player.Profile;
import org.ltzin.utils.StringUtils;

import java.util.Optional;

public final class RoleTag {

    private RoleTag() {}

    public static Optional<Role> getTagRole(String playerName) {
        if (Role.listRoles().isEmpty()) return Optional.empty();

        Profile profile = Profile.getProfile(playerName);
        if (profile == null) return Optional.empty();

        DataContainer dc = profile.getDataContainer("VoidProfile", "role");
        if (dc == null) return Optional.empty();

        String saved = dc.getAsString();
        if (saved == null || saved.isEmpty() || saved.equalsIgnoreCase("Membro")) {
            return Optional.empty();
        }

        Role found = Role.byName(saved);
        if (found.isDefault() && !StringUtils.stripColors(found.getName()).equalsIgnoreCase(saved)) {
            return Optional.empty();
        }

        return Optional.of(found);
    }
}