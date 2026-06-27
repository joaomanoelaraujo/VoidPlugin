package org.ltzin.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class SkinLibrary {

    private static final List<SkinPreset> PRESETS = new ArrayList<>();

    static {
        register("goku",     "§7Goku",     "ewogICJ0aW1lc3RhbXAiIDogMTYyMDY5MDY3OTc5OCwKICAicHJvZmlsZUlkIiA6ICIwNWQ0NTNiZWE0N2Y0MThiOWI2ZDUzODg0MWQxMDY2MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJFY2hvcnJhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE4NDgxMTI1NTZhYzVjZmZhMTM4ZGNjOTgyMWFjYmI5NTZiN2Y3YTU5ZTNjYmExZGY2NGQ3MDlmNTU4ODkwYTAiCiAgICB9CiAgfQp9", "u9y181TIlNSbJoN8+8S7V7/IEtspBmjElEEc2gvEycb6wixHJDHJB51gmOiRaWgOvnNUh/hcQ9qDB+/oBfp+jRof1A7HYPZvQNZyOxZuMmVFLJGD4pwAeJgW21DXbzGBFTEIJL5WBmchjIdKhZ5lXUBlYaqLUaPbSH12UN5tPS9IpGReXRcDPF0CwsuTkwU3agUrPPOb1++0MI4FBdNp54ynK5Ld8gOzZHWCV+1LTWD1YRH6bsvnUZdAuXh2822iNEvNu55kG50vUEEFMxSBeAAo0gyFwcNhWVtCcisR1cL9yjWJJbgSIr+I0kOCtO7tr2m5No+jsv53r/xB8KwcCLTh9SUjOYIzvcqkkkX9wKUR1iouFOUrD52qlYk3aQYzq7e+4aADvSJa/Fpb9OITW6/WN2vTIUtnYUDAi8D+dV9GPMs/ed1cpbEriXI9dlE6bdOccYuOtgODCOvhIc613VI5xLhj3HzHlaJyWu+2Jr6tlzNFwZ3loZF4An6OZAujRq4JAVhLhRy+2QnQZeNxcSkBE7gtAspuTzsRyg50S+yfACeLFn+Gg4cSp8J1isyrb0bqQ2J8S8w6J6DZsKw3EykyB4Ruk0hTOQxRvEFAAKTsKvbb++fvnpdi/LDST8BC5eycdMWDGmJ+9ZwX8HoydAfVs/h0fTvSk1UruYKCdbA=");

    }

    private SkinLibrary() {}

    public static void register(String id, String displayName, String value, String signature) {
        PRESETS.add(new SkinPreset(id, displayName, value, signature));
    }

    public static List<SkinPreset> getAll() {
        return Collections.unmodifiableList(PRESETS);
    }

    public static SkinPreset getById(String id) {
        for (SkinPreset preset : PRESETS) {
            if (preset.getId().equalsIgnoreCase(id)) {
                return preset;
            }
        }
        return null;
    }

    public static int size() {
        return PRESETS.size();
    }
}