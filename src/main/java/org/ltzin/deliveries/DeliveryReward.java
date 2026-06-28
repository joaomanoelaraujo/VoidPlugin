package org.ltzin.deliveries;

import org.bukkit.Bukkit;
import org.ltzin.player.Profile;


public class DeliveryReward {

    private final RewardType type;
    private final Object[]   values;

    public DeliveryReward(String reward) {
        if (reward == null || reward.isEmpty()) {
            this.type   = RewardType.COMANDO;
            this.values = new Object[]{"tell {name} §cEntrega sem recompensa configurada."};
            return;
        }

        String[] splitter = reward.split(">", 2);
        if (splitter.length < 2) {
            this.type   = RewardType.COMANDO;
            this.values = new Object[]{"tell {name} §cRecompensa inválida: " + reward};
            return;
        }

        RewardType parsed = RewardType.from(splitter[0]);

        if (parsed == null || splitter[1].split(":").length < parsed.getParameters()) {
            Bukkit.getLogger().warning("[DeliveryReward] Recompensa inválida: \"" + reward + "\"");
            this.type   = RewardType.COMANDO;
            this.values = new Object[]{"tell {name} §cPrêmio \"" + reward + "\" inválido!"};
            return;
        }

        RewardType   resolvedType;
        Object[]     resolvedValues;

        try {
            resolvedValues = parsed.parseValues(splitter[1]);
            resolvedType   = parsed;
        } catch (Exception ex) {
            ex.printStackTrace();
            resolvedType   = RewardType.COMANDO;
            resolvedValues = new Object[]{"tell {name} §cErro ao processar prêmio: " + reward};
        }

        this.type   = resolvedType;
        this.values = resolvedValues;
    }

    public void dispatch(Profile profile) {
        if (profile == null || profile.getPlayer() == null) {
            Bukkit.getLogger().warning("[DeliveryReward] Profile ou player null ao dar recompensa!");
            return;
        }

        try {
            switch (type) {
                case COMANDO: {
                    String command = ((String) values[0]).replace("{name}", profile.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    break;
                }

                case CASH: {
                    profile.addCash((long) values[0]);
                    break;
                }

                default: {
                    if (type.name().endsWith("_COINS")) {
                        dispatchCoins(profile);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[DeliveryReward] Erro ao executar recompensa "
                    + type + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void dispatchCoins(Profile profile) {

        String gameName = type.name().replace("_COINS", "").toLowerCase();
        String command  = gameName + " addcoins " + profile.getName()
                + " " + ((Double) values[0]).intValue();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public String toString() {
        return "DeliveryReward{type=" + type
                + ", values=" + java.util.Arrays.toString(values) + "}";
    }


    public enum RewardType {
        COMANDO(1),
        CASH(1),
        BEDWARS_COINS(1),
        SKYWARS_COINS(1);

        private final int parameters;

        RewardType(int parameters) {
            this.parameters = parameters;
        }

        public static RewardType from(String name) {
            for (RewardType t : values()) {
                if (t.name().equalsIgnoreCase(name)) return t;
            }
            return null;
        }

        public int getParameters() {
            return parameters;
        }

        public Object[] parseValues(String value) throws Exception {
            switch (this) {
                case COMANDO:
                    return new Object[]{value};
                case CASH:
                    return new Object[]{Long.parseLong(value.trim())};
                case BEDWARS_COINS:
                case SKYWARS_COINS:
                    return new Object[]{Double.parseDouble(value.trim())};
                default:
                    throw new Exception("Tipo de recompensa sem parseValues: " + this);
            }
        }
    }
}