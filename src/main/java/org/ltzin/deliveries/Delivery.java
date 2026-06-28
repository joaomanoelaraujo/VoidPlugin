package org.ltzin.deliveries;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ltzin.Main;
import org.ltzin.database.storage.implementation.StorageImplementation;
import org.ltzin.player.Profile;
import org.ltzin.utils.BukkitUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Delivery {

    private static final List<Delivery> DELIVERIES = new ArrayList<>();

    public static void setupDeliveries() {
        DELIVERIES.clear();

        org.bukkit.configuration.file.FileConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.File(Main.getInstance().getDataFolder(), "deliveries.yml")
                );

        if (!config.isConfigurationSection("deliveries")) return;

        for (String key : config.getConfigurationSection("deliveries").getKeys(false)) {
            String path = "deliveries." + key + ".";

            int    days       = config.getInt(path + "days", 1);
            int    slot       = config.getInt(path + "slot", -1);
            String permission = config.getString(path + "permission", "");
            String icon       = config.getString(path + "icon", "CHEST : 1 : name>Entrega");
            String name       = config.getString(path + "name", key);
            String message    = config.getString(path + "message", "");

            List<DeliveryReward> rewards = new ArrayList<>();
            for (String reward : config.getStringList(path + "rewards")) {
                rewards.add(new DeliveryReward(reward));
            }

            DELIVERIES.add(new Delivery(days, slot, permission, rewards, icon, name, message));
        }
    }

    public static Collection<Delivery> listDeliveries() {
        return Collections.unmodifiableList(DELIVERIES);
    }

    public enum ClaimState {
        AVAILABLE,
        ON_COOLDOWN,
        NO_PERMISSION
    }

    private final long id;
    private final int  days;
    private final int  slot;   // -1 = posição automática pelo índice
    private final String permission;
    private final List<DeliveryReward> rewards;
    private final String icon;
    private final String name;
    private final String message;

    public Delivery(int days, int slot, String permission,
                    List<DeliveryReward> rewards,
                    String icon, String name, String message) {
        this.id         = DELIVERIES.size();
        this.days       = days;
        this.slot       = slot;
        this.permission = permission == null ? "" : permission;
        this.rewards    = rewards;
        this.icon       = icon;
        this.name       = name;
        this.message    = BukkitUtils.deserializeItemStack("STONE : 1") != null
                ? message.replace("&", "§")
                : message;
    }

    private DeliveriesContainer getContainer(Profile profile) {
        return profile.getContainer("VoidProfile", "delivery_claims", DeliveriesContainer.class);
    }

    public ClaimState getState(Profile profile) {
        Player player = profile.getPlayer();

        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            return ClaimState.NO_PERMISSION;
        }

        if (getContainer(profile).alreadyClaimed(id, getCooldownMillis())) {
            return ClaimState.ON_COOLDOWN;
        }

        return ClaimState.AVAILABLE;
    }

    public boolean canClaim(Profile profile) {
        return getState(profile) == ClaimState.AVAILABLE;
    }

    public boolean claim(Profile profile) {
        if (!canClaim(profile)) return false;

        for (DeliveryReward reward : rewards) {
            reward.dispatch(profile);
        }

        getContainer(profile).claim(id);

        StorageImplementation storage = Main.getInstance().getDatabaseManager().getStorage();
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                Main.getInstance(),
                () -> profile.save(storage)
        );

        return true;
    }

    public ItemStack getIcon(Profile profile) {
        ClaimState state     = getState(profile);
        DeliveriesContainer container = getContainer(profile);

        String color;
        String extraDesc = "";

        switch (state) {
            case AVAILABLE:
                color = "&a";
                break;

            case ON_COOLDOWN:
                color = "&c";
                long remaining = container.getTimeUntilNextClaim(id, getCooldownMillis());
                extraDesc = "\n \n&7Coletável em &f" + formatTime(remaining) + "&7.";
                break;

            case NO_PERMISSION:
            default:
                color = "&c";
                extraDesc = "\n \n&cVocê não tem permissão para esta entrega.";
                break;
        }

        String iconStr = icon
                .replace("{color}", color)
                .replace("{name}", name != null ? name : "")
                + extraDesc;

        ItemStack item = BukkitUtils.deserializeItemStack(iconStr);

        if (state == ClaimState.ON_COOLDOWN) {
            item = swapToEmptyVariant(item);
        }

        return item;
    }

    private ItemStack swapToEmptyVariant(ItemStack item) {
        if (item == null) return null;
        switch (item.getType()) {
            case STORAGE_MINECART:
                item.setType(Material.MINECART);
                item.setDurability((short) 0);
                break;
            case POTION:
                item.setType(Material.GLASS_BOTTLE);
                item.setDurability((short) 0);
                break;
            default:
                break;
        }
        return item;
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "0s";
        long days    = TimeUnit.MILLISECONDS.toDays(millis);
        long hours   = TimeUnit.MILLISECONDS.toHours(millis)   % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis)  % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)  % 60;

        StringBuilder sb = new StringBuilder();
        if (days    > 0) sb.append(days).append("d ");
        if (hours   > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    public long   getId()           { return id; }
    public String getName()         { return name; }
    public String getMessage()      { return message; }
    public String getPermission()   { return permission; }
    public long   getCooldownMillis() { return TimeUnit.DAYS.toMillis(days); }
    public int    getSlot()         { return slot; }
    public List<DeliveryReward> listRewards() { return rewards; }
}