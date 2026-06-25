package org.ltzin.database.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ltzin.database.data.interfaces.AbstractContainer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DataContainer {

    private Object value;
    private boolean updated;
    private Map<Class<? extends AbstractContainer>, AbstractContainer> containerMap = new HashMap<>();

    public DataContainer(Object value) {
        this.value = value;
    }

    public void gc() {
        this.value = null;
        this.containerMap.values().forEach(AbstractContainer::gc);
        this.containerMap.clear();
        this.containerMap = null;
    }

    public void set(Object value) {
        if (this.value == null || !this.value.equals(value)) {
            this.updated = true;
        }
        this.value = value;
    }

    public void addInt(int amount)       { this.set(getAsInt() + amount); }
    public void addLong(long amount)     { this.set(getAsLong() + amount); }
    public void addDouble(double amount) { this.set(getAsDouble() + amount); }
    public void removeInt(int amount)    { this.set(getAsInt() - amount); }
    public void removeLong(long amount)  { this.set(getAsLong() - amount); }
    public void removeDouble(double amount) { this.set(getAsDouble() - amount); }

    public Object get()           { return this.value; }
    public String getAsString()   { return this.value.toString(); }
    public int    getAsInt()      { return Integer.parseInt(getAsString()); }
    public long   getAsLong()     { return Long.parseLong(getAsString()); }
    public double getAsDouble()   { return Double.parseDouble(getAsString()); }
    public boolean getAsBoolean() { return Boolean.parseBoolean(getAsString()); }

    public JSONObject getAsJsonObject() {
        try {
            return (JSONObject) new JSONParser().parse(getAsString());
        } catch (Exception ex) {
            throw new IllegalArgumentException("\"" + value + "\" is not a JSONObject", ex);
        }
    }

    public JSONArray getAsJsonArray() {
        try {
            return (JSONArray) new JSONParser().parse(getAsString());
        } catch (Exception ex) {
            throw new IllegalArgumentException("\"" + value + "\" is not a JSONArray", ex);
        }
    }

    public boolean isUpdated()              { return updated; }
    public void setUpdated(boolean updated) { this.updated = updated; }

    @SuppressWarnings("unchecked")
    public <T extends AbstractContainer> T getContainer(Class<T> containerClass) {
        if (!this.containerMap.containsKey(containerClass)) {
            try {
                Constructor<T> constructor = containerClass.getDeclaredConstructor(DataContainer.class);
                constructor.setAccessible(true);
                this.containerMap.put(containerClass, constructor.newInstance(this));
            } catch (Exception ex) {
                throw new RuntimeException("Falha ao instanciar container: " + containerClass.getSimpleName(), ex);
            }
        }
        return (T) this.containerMap.get(containerClass);
    }
}