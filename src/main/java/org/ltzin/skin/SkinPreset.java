package org.ltzin.skin;

public class SkinPreset {

    private final String   id;
    private final String   displayName;
    private final SkinData skinData;

    public SkinPreset(String id, String displayName, String value, String signature) {
        this.id          = id;
        this.displayName = displayName;
        this.skinData    = new SkinData(value, signature);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SkinData getSkinData() {
        return skinData;
    }
}