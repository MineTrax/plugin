package com.xinecraft.minetrax.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    public static Class<?> getNmsClass(String name) {
        String className = "net.minecraft.server." + name;
        if(getClass(className) != null) return getClass(className);

        className = "net.minecraft.server." + getVersion() + "." + name;
        return getClass(className);
    }

    public static Class<?> getCbClass(String name) {
        String className = "org.bukkit.craftbukkit." + getVersion() + "." + name;
        return getClass(className);
    }

    public static Class<?> getUtilClass(String name) {
        try {
            return Class.forName(name); //Try before 1.8 first
        } catch (ClassNotFoundException ex) {
            try {
                return Class.forName("net.minecraft.util." + name); //Not 1.8
            } catch (ClassNotFoundException ex2) {
                return null;
            }
        }
    }

    public static String getVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static Object getHandle(Object wrapper) {
        Method getHandle = makeMethod(wrapper.getClass(), "getHandle");
        return callMethod(getHandle, wrapper);
    }

    //Utils
    public static Method makeMethod(Class<?> clazz, String methodName, Class<?>... paramaters) {
        try {
            return clazz.getDeclaredMethod(methodName, paramaters);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(Method method, Object instance, Object... paramaters) {
        if (method == null) throw new RuntimeException("No such method");
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> makeConstructor(Class<?> clazz, Class<?>... paramaterTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(paramaterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T callConstructor(Constructor<T> constructor, Object... paramaters) {
        if (constructor == null) throw new RuntimeException("No such constructor");
        constructor.setAccessible(true);
        try {
            return (T) constructor.newInstance(paramaters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field makeField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object instance) {
        if (field == null) throw new RuntimeException("No such field");
        field.setAccessible(true);
        try {
            return (T) field.get(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        if (field == null) throw new RuntimeException("No such field");
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static <T> Class<? extends T> getClass(String name, Class<T> superClass) {
        try {
            return Class.forName(name).asSubclass(superClass);
        } catch (ClassCastException | ClassNotFoundException ex) {
            return null;
        }
    }

    // Fuzzy reflection

    public static Field getOnlyField(Class<?> toGetFrom, Class<?> type) {
        Field only = null;
        for (Field field : toGetFrom.getDeclaredFields()) {
            if (!type.isAssignableFrom(field.getClass())) continue;
            Preconditions.checkArgument(only == null, "More than one field of type %s on %s: %s and %s", type.getSimpleName(), toGetFrom.getSimpleName(), field.getName(), only.getName());
            only = field;
        }
        return only;
    }

    public static Method getOnlyMethod(Class<?> toGetFrom, Class<?> returnType, Class<?>... paramSpec) {
        Method only = null;
        for (Method method : toGetFrom.getDeclaredMethods()) {
            if (!returnType.isAssignableFrom(method.getReturnType())) continue;
            if (!isParamsMatchSpec(method.getParameterTypes(), paramSpec)) continue;
            Preconditions.checkArgument(only == null, "More than one method matching spec on %s" + ((only.getName().equals(method.getName())) ? "" : ": " + only.getName() + " " + method.getName()), toGetFrom.getSimpleName());
            only = method;
        }
        return only;
    }

    public static boolean isParamsMatchSpec(Class<?>[] parameters, Class<?>... paramSpec) {
        if (parameters.length > paramSpec.length) return false;
        for (int i = 0; i < paramSpec.length; i++) {
            Class<?> spec = paramSpec[i];
            if (spec == null) continue;
            Class parameter = parameters[i];
            if (!spec.isAssignableFrom(parameter)) return false;
        }
        return true;
    }

}
