package org.ltzin.utils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectionUtil {

    private ReflectionUtil() {}

    private static final Map<String, Method> CACHE = new ConcurrentHashMap<>();

    public static void tryInvoke(Object target, String methodName, Class<?> paramType, Object arg) {
        if (target == null) return;
        String key = target.getClass().getName() + "#" + methodName + "(" + paramType.getName() + ")";
        try {
            Method method = CACHE.get(key);
            if (method == null) {
                method = target.getClass().getMethod(methodName, paramType);
                method.setAccessible(true);
                CACHE.put(key, method);
            }
            method.invoke(target, arg);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {

        }
    }
}