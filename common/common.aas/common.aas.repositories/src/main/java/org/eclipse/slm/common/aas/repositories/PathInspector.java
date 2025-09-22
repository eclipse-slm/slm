package org.eclipse.slm.common.aas.repositories;

import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PathInspector {

    public static String getRequestPathForMethod(Class<?> controllerClass, String methodName) {
        Set<Method> allMethods = getAllMethods(controllerClass);
        String classPath = getClassRequestMapping(controllerClass);

        for (Method method : allMethods) {
            if (method.getName().equals(methodName)) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null && methodMapping.value().length > 0) {
                    return classPath + methodMapping.value()[0];
                }
            }
        }
        return null;
    }

    private static Set<Method> getAllMethods(Class<?> clazz) {
        Set<Method> methods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            methods.addAll(getAllMethods(superClass));
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            methods.addAll(getAllMethods(iface));
        }
        return methods;
    }

    private static String getClassRequestMapping(Class<?> clazz) {
        RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
            return mapping.value()[0];
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            String superPath = getClassRequestMapping(superClass);
            if (!superPath.isEmpty()) return superPath;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            String ifacePath = getClassRequestMapping(iface);
            if (!ifacePath.isEmpty()) return ifacePath;
        }
        return "";
    }

}
