package org.ltzin.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.ltzin.utils.ReflectionUtil;

/**
 * Representa UMA linha de um holograma. Cada linha é um ArmorStand real,
 * porém completamente configurado para ser "invisível" e não interagível:
 * sem gravidade, sem base, sem braços, sem colisão, invulnerável.
 *
 * Usamos ArmorStand (e não TextDisplay) porque ArmorStand existe desde a
 * 1.8, enquanto TextDisplay só existe a partir da 1.19.4. Isso garante
 * compatibilidade universal com uma única implementação.
 */
public class HologramLine {

    private static final double LINE_SPACING = 0.25; // espaço vertical entre linhas

    private ArmorStand stand;
    private String text;

    public HologramLine(String text) {
        this.text = text;
    }

    /** Spawna (ou re-spawna) a entidade na localização informada. */
    public void spawn(Location location) {
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
        stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        // Confirmados na interface ArmorStand do seu Spigot 1.8.8 -> chamada direta:
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);
        stand.setMarker(true);
        
        ReflectionUtil.tryInvoke(stand, "setAI", boolean.class, false);
        ReflectionUtil.tryInvoke(stand, "setCollidable", boolean.class, false);   // 1.9+
        ReflectionUtil.tryInvoke(stand, "setInvulnerable", boolean.class, true);
        ReflectionUtil.tryInvoke(stand, "setSilent", boolean.class, true);
        ReflectionUtil.tryInvoke(stand, "setPersistent", boolean.class, false);
    }

    public void setText(String text) {
        this.text = text;
        if (stand != null && !stand.isDead()) {
            stand.setCustomName(text);
        }
    }

    public String getText() {
        return text;
    }

    public void teleport(Location location) {
        if (stand != null && !stand.isDead()) {
            stand.teleport(location);
        }
    }

    public void remove() {
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
        stand = null;
    }

    public static double getLineSpacing() {
        return LINE_SPACING;
    }

    public ArmorStand getEntity() {
        return stand;
    }
}