package org.ltzin.database.data;

import org.json.simple.JSONObject;
import org.ltzin.database.data.interfaces.AbstractContainer;
import org.ltzin.player.preferences.PlayerPreference;


public class PreferencesContainer extends AbstractContainer {

    private JSONObject json;

    public PreferencesContainer(DataContainer dataContainer) {
        super(dataContainer);
        this.json = dataContainer.getAsJsonObject();
    }


    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T get(PlayerPreference preference) {
        Object raw = json.get(preference.getKey());

        long ordinal = (raw instanceof Number)
                ? ((Number) raw).longValue()
                : preference.getDefaultOrdinal();

        Enum<?>[] constants = preference.getEnumClass().getEnumConstants();

        if (ordinal < 0 || ordinal >= constants.length) {
            return (T) preference.getDefaultValue();
        }

        return (T) constants[(int) ordinal];
    }


    public boolean isEnabled(PlayerPreference preference) {
        return get(preference).ordinal() == 0;
    }


    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> void set(PlayerPreference preference, T value) {
        json.put(preference.getKey(), (long) value.ordinal());
        flush();
    }


    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T toggle(PlayerPreference preference) {
        T current = get(preference);

        T next;
        try {
            next = (T) current.getClass().getMethod("next").invoke(current);
        } catch (Exception ex) {
            throw new RuntimeException("O enum " + current.getClass().getSimpleName()
                    + " não possui o método next()", ex);
        }

        set(preference, next);
        return next;
    }


    private void flush() {
        dataContainer.set(json.toJSONString());
    }

    public String toJson() {
        return json.toJSONString();
    }


    @Override
    public void gc() {
        this.json = null;
        super.gc();
    }
}