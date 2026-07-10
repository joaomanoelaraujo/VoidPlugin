package org.ltzin.hologram;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Um holograma = um conjunto ordenado de linhas (HologramLine), empilhadas
 * verticalmente a partir de um ponto base (a linha mais alta primeiro).
 */
public class Hologram {

    private final String id;
    private Location baseLocation;
    private final List<HologramLine> lines = new ArrayList<>();

    public Hologram(String id, Location baseLocation) {
        this.id = id;
        this.baseLocation = baseLocation.clone();
    }

    public String getId() {
        return id;
    }

    public Location getBaseLocation() {
        return baseLocation.clone();
    }

    public List<HologramLine> getLines() {
        return lines;
    }

    public void addLine(String text) {
        HologramLine line = new HologramLine(text);
        lines.add(line);
        spawnAll();
    }

    public void setLine(int index, String text) {
        if (index < 0 || index >= lines.size()) return;
        lines.get(index).setText(text);
    }

    public void removeLine(int index) {
        if (index < 0 || index >= lines.size()) return;
        lines.get(index).remove();
        lines.remove(index);
        spawnAll(); // reajusta posições das linhas restantes
    }

    /** Recalcula a posição de cada linha e (re)spawna todas. */
    public void spawnAll() {
        Location current = baseLocation.clone();
        for (HologramLine line : lines) {
            line.spawn(current.clone());
            current.subtract(0, HologramLine.getLineSpacing(), 0);
        }
    }

    public void moveTo(Location newLocation) {
        this.baseLocation = newLocation.clone();
        spawnAll();
    }

    public void remove() {
        for (HologramLine line : lines) {
            line.remove();
        }
        lines.clear();
    }
}
