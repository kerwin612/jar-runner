package io.github.kerwin612.jar_runner;

import io.github.kerwin612.jar_runner.loader.MavenJarLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class JarRunner {

    public static JarRunner load(Path loadedPath, boolean override, Path... jars) throws IOException {
        return new JarRunner(loadedPath, override, jars);
    }

    public static JarRunner load(boolean override, Path... jars) throws IOException {
        return load(null, override, jars);
    }

    public static JarRunner load(Path... jars) throws IOException {
        return load(false, jars);
    }

    private URLClassLoader classLoader;

    private JarRunner(Path loadedPath, boolean override, Path... jars) throws IOException {

        List<URL> urlList = new ArrayList<>();
        for (int i = 0, j = jars.length; i < j; i++) {
            urlList.addAll(Arrays.asList(new MavenJarLoader().load(jars[i], loadedPath, override)));
        }

        classLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]), ClassLoader.getSystemClassLoader()) {

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass == null) {
                    try {
                        if (loadedClass == null) {
                            loadedClass = findClass(name);
                        }
                    } catch (ClassNotFoundException e) {
                        loadedClass = super.loadClass(name, resolve);
                    }
                }

                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }


            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                List<URL> allRes = new LinkedList<>();

                Enumeration<URL> thisRes = findResources(name);
                if (thisRes != null) {
                    while (thisRes.hasMoreElements()) {
                        allRes.add(thisRes.nextElement());
                    }
                }

                Enumeration<URL> parentRes = super.findResources(name);
                if (parentRes != null) {
                    while (parentRes.hasMoreElements()) {
                        allRes.add(parentRes.nextElement());
                    }
                }

                return new Enumeration<URL>() {
                    Iterator<URL> it = allRes.iterator();

                    @Override
                    public boolean hasMoreElements() {
                        return it.hasNext();
                    }

                    @Override
                    public URL nextElement() {
                        return it.next();
                    }
                };
            }

            @Override
            public URL getResource(String name) {
                URL res = findResource(name);
                if (res == null) {
                    res = super.getResource(name);
                }
                return res;
            }

        };

    }

    public <T> T run(String className, String methodName, Object... args) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
        Class<?> externalClass = classLoader.loadClass(className);
        Class[] argsClasses = new Class[args.length];
        for (int i = 0, j = argsClasses.length; i < j; i++) {
            argsClasses[i] = (args[i]).getClass();
        }
        Method method = externalClass.getMethod(methodName, argsClasses);
        return (T) method.invoke(externalClass.newInstance(), args);
    }

}
