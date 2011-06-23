package de.fu_berlin.inf.dpp.stf;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TestLinkFinder {

    private static final String STF_TEST_CASE_PACKAGE = "de.fu_berlin.inf.dpp.stf.test";
    private static URLClassLoader loader;
    private static File baseDirectory;
    private static Class<?> testLinkAnnotation;

    public static void main(String... strings) throws IOException,
        ClassNotFoundException {

        loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        testLinkAnnotation = Class
            .forName("de.fu_berlin.inf.dpp.stf.annotation.TestLink");

        String className = TestLinkFinder.class.getName().replace(".", "/")
            .concat(".class");

        String location = loader.getResource(className).toString();

        int idx = location.indexOf('!');

        boolean isJarFile = false;

        File file;

        if (idx != -1) {
            // jar:file/ .... !
            location = location.substring(4, idx);
            isJarFile = true;
            URI fileLocation = URI.create(location);
            file = new File(fileLocation);
        } else {
            baseDirectory = new File(URI.create(location));
            File classFile = new File(className);

            baseDirectory = new File(baseDirectory.getPath()
                .substring(
                    0,
                    baseDirectory.getPath().length()
                        - classFile.getPath().length()));

            file = baseDirectory;
        }

        if (isJarFile)
            readJarFile(file);
        else
            readDirectory(file);
    }

    private static File makeRelative(File file) {
        return new File(file.getPath().substring(
            baseDirectory.getPath().length() + 1));
    }

    private static void readDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory())
                readDirectory(file);

            String filename = file.getName();

            if (filename.endsWith(".class")) {
                filename = makeRelative(file).getPath();
                String className = filename.replace("\\", "/")
                    .replace("/", ".").replaceAll("\\.class", "");

                if (!className.contains(STF_TEST_CASE_PACKAGE))
                    continue;

                Class<?> clazz;

                try {
                    clazz = loader.loadClass(className);
                } catch (Throwable t) {
                    System.err.println("Error: " + className + " "
                        + t.getMessage());
                    continue;
                }

                processAnnotation(clazz);
            }
        }
    }

    private static void readJarFile(File file) throws IOException {
        JarFile jarFile = new JarFile(file);

        for (Enumeration<JarEntry> entries = jarFile.entries(); entries
            .hasMoreElements();) {
            JarEntry entry = entries.nextElement();

            if (entry.isDirectory())
                continue;

            String entryName = entry.getName();

            if (!entryName.endsWith(".class"))
                continue;

            String className = entryName.replace("\\", "/").replace("/", ".")
                .replaceAll("\\.class", "");

            if (!className.contains(STF_TEST_CASE_PACKAGE))
                continue;

            Class<?> clazz;

            try {
                clazz = loader.loadClass(className);
            } catch (Throwable t) {
                System.err
                    .println("Error: " + className + " " + t.getMessage());
                continue;
            }

            processAnnotation(clazz);
        }

        jarFile.close();
    }

    public static void processAnnotation(Class<?> clazz) {

        try {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (testLinkAnnotation.isAssignableFrom(annotation.getClass())) {
                    Method id = annotation.getClass().getMethod("id");
                    System.out.print(id.invoke(annotation) + " ");
                    System.out.println(clazz.getName());
                }
            }
        } catch (Throwable t) {
            System.err.println("Error while reading annotations: " + clazz
                + " " + t.getMessage());
        }
    }
}