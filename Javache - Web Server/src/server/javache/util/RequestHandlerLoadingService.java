package server.javache.util;

import server.javache.WebConstants;
import server.javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class RequestHandlerLoadingService {

    private static final String LIB_FOLDER_PATH = WebConstants.SERVER_ROOT_COMPILED_FOLDER_PATH.concat("lib/");

    private Set<RequestHandler> requestHandlers;

    public RequestHandlerLoadingService() {
    }

    public Set<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableSet(this.requestHandlers);
    }

    private String getFileNameWithoutExtension(File file) {
        return file.getName()
                .substring(0, file.getName().indexOf("."));
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }

    private void loadRequestHandler(Class<?> requestHandlerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RequestHandler requestHandlerObject =
                (RequestHandler) requestHandlerClass
                        .getDeclaredConstructor(String.class)
                        .newInstance(WebConstants.SERVER_ROOT_COMPILED_FOLDER_PATH);

        this.requestHandlers.add(requestHandlerObject);
    }

    private void loadJarFile(JarFile jarFile, String canonicalPath) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();

            if (currentEntry.isDirectory() || !currentEntry.getRealName().endsWith(".class")) continue;

            URL[] urls = new URL[] {
                    new URL("jar:file:".concat(canonicalPath).concat("!/"))
            };

            URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(urlClassLoader);

            String className = currentEntry.getRealName()
                    .replace(".class", "")
                    .replace("/", ".");

            Class currentClassFile = urlClassLoader.loadClass(className);

            if (RequestHandler.class.isAssignableFrom(currentClassFile)) {
                this.loadRequestHandler(currentClassFile);
            }
        }
    }

    private void loadLibraryFiles(Set<String> requestHandlerPriority) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        File libraryFolder = new File(LIB_FOLDER_PATH);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException("Library Folder does not exist or is not a directory.");
        }

        List<File> jarLibFiles = Arrays.stream(libraryFolder.listFiles())
                .filter(this::isJarFile)
                .collect(Collectors.toList());

        for (String currentRequestHandlerName : requestHandlerPriority) {
            File jarFile = jarLibFiles.stream()
                    .filter(x -> this.getFileNameWithoutExtension(x)
                            .equals(currentRequestHandlerName)
                    )
                    .findFirst()
                    .orElse(null);

            if (jarFile != null) {
                JarFile fileAsJar = new JarFile(jarFile.getCanonicalPath());
                this.loadJarFile(fileAsJar, jarFile.getCanonicalPath());
            }
        }
    }

    public void loadRequestHandler(Set<String> requestHandlerPriority) throws NoSuchMethodException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        this.requestHandlers = new LinkedHashSet<>();

        this.loadLibraryFiles(requestHandlerPriority);
    }
}