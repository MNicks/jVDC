package de.nikxs.digitalstrom.vdc.server.util;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * ...
 */
public class ReflectionUtil {
    /**
     * Get all the instance methods of a class
     *
     * @param clazz
     * @return
     */
    public static Method[] getAllInstanceMethods(final Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<Method> methods = new ArrayList<Method>();
        for (Class<?> itr = clazz; hasSuperClass(itr);) {
            for (Method method : itr.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    methods.add(method);
                }
            }
            itr = itr.getSuperclass();
        }

        return methods.toArray(new Method[methods.size()]);

    }

    /**
     * Determine whether a class contains a parent class or interface
     *
     * @param clazz
     * @return
     */
    public static boolean hasSuperClass(Class<?> clazz) {
        return (clazz != null) && !clazz.equals(Object.class);
    }

    /**
     * Determine whether a class is a void type
     *
     * @param cls
     * @return
     */
    public static boolean isVoid(Class<?> cls) {
        if (cls == void.class) {
            return true;
        }
        return false;
    }
}
