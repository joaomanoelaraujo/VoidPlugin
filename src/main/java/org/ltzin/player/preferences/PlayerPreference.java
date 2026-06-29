package org.ltzin.player.preferences;


import org.ltzin.player.enums.*;

public enum PlayerPreference {

    PLAYER_VISIBILITY  ("pv",  PlayerVisibility.class,    0),
    PRIVATE_MESSAGES   ("pm",  PrivateMessages.class,     0),
    MENTIONS           ("mn",  Mentions.class,            0),
    CHAT_MESSAGES      ("ch",  ChatMessages.class,        0),
    WORD_FILTER        ("wf",  WordFilter.class,          0),
    LOBBY_JOIN_MESSAGES("lm",  LobbyJoinMessages.class,   0),
    TIME_OF_DAY        ("td",  TimeOfDay.class,           0),
    FLY_ON_JOIN        ("fly", FlyOnJoin.class,           1),
    AUTO_QUEUE         ("aq",  AutoQueue.class,           1),
    MAP_RATING         ("mr",  MapRating.class,           1),
    BLOOD_PARTICLES    ("bp",  BloodParticles.class,      1),
    MAP_SELECTOR       ("ms",  MapSelector.class,         1),
    GUILD_CHAT         ("gc",  GuildChat.class,           0),
    GUILD_NOTIFICATIONS("gn",  GuildNotifications.class,  0);

    private final String     key;
    private final Class<? extends Enum<?>> enumClass;
    private final int        defaultOrdinal;

    PlayerPreference(String key, Class<? extends Enum<?>> enumClass, int defaultOrdinal) {
        this.key            = key;
        this.enumClass      = enumClass;
        this.defaultOrdinal = defaultOrdinal;
    }

    public String getKey()                          { return key; }
    public Class<? extends Enum<?>> getEnumClass()  { return enumClass; }
    public int getDefaultOrdinal()                  { return defaultOrdinal; }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getDefaultValue() {
        return (T) enumClass.getEnumConstants()[defaultOrdinal];
    }

    public static PlayerPreference byKey(String key) {
        for (PlayerPreference pref : values()) {
            if (pref.key.equals(key)) return pref;
        }
        return null;
    }
}