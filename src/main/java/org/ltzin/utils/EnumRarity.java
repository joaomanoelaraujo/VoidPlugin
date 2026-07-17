package org.ltzin.utils;

import java.util.concurrent.ThreadLocalRandom;

public enum EnumRarity {

    COMUM("§fComum", 50),
    INCOMUM("§aIncomum", 25),
    RARO("§9Raro", 15),
    EPICO("§5Épico", 7),
    LENDARIO("§6Lendário", 2),
    MITICO("§dMítico", 1),
    DIVINO("§cDivino", 0);

  private static final EnumRarity[] VALUES = values();
  private final String name;
  private final int percentage;

  private EnumRarity(String name, int percentage) {
    this.name = name;
    this.percentage = percentage;
  }

  public static EnumRarity getRandomRarity() {
    int random = ThreadLocalRandom.current().nextInt(100);
      for (EnumRarity rarity : VALUES) {
          if (random <= rarity.percentage) {
              return rarity;
          }
      }

    return COMUM;
  }

  public static EnumRarity fromName(String name) {
      for (EnumRarity rarity : VALUES) {
          if (rarity.name().equalsIgnoreCase(name)) {
              return rarity;
          }
      }

    return COMUM;
  }

  public String getName() {
    return this.name;
  }

  public String getColor() {
    return StringUtils.getFirstColor(this.getName());
  }

  public String getTagged() {
    return this.getColor() + "[" + StringUtils.stripColors(this.getName()) + "]";
  }

  private static EnumRarity[] $values() {
    return new EnumRarity[]{COMUM, INCOMUM, RARO, EPICO, LENDARIO, MITICO, DIVINO};
  }
}
