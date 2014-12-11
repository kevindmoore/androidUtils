package com.mastertechsoftware.util;

/**
 * Helper class to store/retrieve class loader
 */
public class ClassLoaderHelper {
    static ClassLoader classLoader;

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public static void setClassLoader(ClassLoader classLoader) {
        ClassLoaderHelper.classLoader = classLoader;
    }
}
