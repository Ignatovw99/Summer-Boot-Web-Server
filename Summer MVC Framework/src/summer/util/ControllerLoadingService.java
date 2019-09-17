package summer.util;

import summer.api.Controller;
import summer.api.GetMapping;
import summer.api.PostMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ControllerLoadingService {

    private Map<String, Map<String, ControllerActionPair>> controllerActionsByRouteAndRequestMethod;

    private void loadController(Class controllerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (controllerClass == null
                || Arrays.stream(controllerClass.getAnnotations())
                .noneMatch(a -> a.annotationType()
                        .getSimpleName()
                        .equals(Controller.class.getSimpleName()))) {
            return;
        }

        Object controllerObject = controllerClass
                .getDeclaredConstructor()
                .newInstance();

        Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class))
                .forEach(action -> {
                    if (action.isAnnotationPresent(GetMapping.class)) {
                        this.controllerActionsByRouteAndRequestMethod.get("GET")
                                .put(
                                        PathFormatter.formatPath(action.getAnnotation(GetMapping.class).route()),
                                        new ControllerActionPair(controllerObject, action)
                                );
                    } else {
                        this.controllerActionsByRouteAndRequestMethod.get("POST")
                                .put(
                                        PathFormatter.formatPath(action.getAnnotation(PostMapping.class).route()),
                                        new ControllerActionPair(controllerObject, action)
                                );
                    }
                });

        for (Map.Entry<String, ControllerActionPair> entry : controllerActionsByRouteAndRequestMethod.get("GET").entrySet()) {
            ControllerActionPair cap = entry.getValue();
            System.out.println(cap.getController());
            System.out.println(cap.getAction());
        }

        for (Map.Entry<String, ControllerActionPair> entry : controllerActionsByRouteAndRequestMethod.get("POST").entrySet()) {
            ControllerActionPair cap = entry.getValue();
            System.out.println(cap.getController());
            System.out.println(cap.getAction());
        }

    }

    private void loadClass(File currentFile, URLClassLoader classLoader, String packageName) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (currentFile.isDirectory()) {
            for (File childFile : currentFile.listFiles()) {
                this.loadClass(
                        childFile,
                        classLoader,
                        packageName.concat(currentFile.getName()).concat(".")
                );
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) return;

            String className = packageName.replace("classes.", "").concat(
                    currentFile.getName()
                    .replace(".class", "")
                    .replace("/", ".")
            );

            Class currentClassFile = classLoader.loadClass(className);
            this.loadController(currentClassFile);
        }
    }

    private void loadApplicationClasses(String classesRootFolderPath) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        File classesRootDirectory = new File(classesRootFolderPath);

        if (!classesRootDirectory.exists() || !classesRootDirectory.isDirectory()) {
            return;
        }

        URL[] urls = new URL[] { new URL("file:/".concat(classesRootDirectory.getCanonicalPath().concat(File.separator))) };
        URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        this.loadClass(classesRootDirectory, classLoader, "");
    }

    private void initControllersMap() {
        this.controllerActionsByRouteAndRequestMethod = new HashMap<>() {{
            put("GET", new HashMap<>());
            put("POST", new HashMap<>());
        }};
    }

    public Map<String, Map<String, ControllerActionPair>> getLoadedControllersAndActions() {
        return this.controllerActionsByRouteAndRequestMethod;
    }

    public void loadControllerActionHandlers(String applicationClassesFolderPath) throws NoSuchMethodException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        this.initControllersMap();
        this.loadApplicationClasses(applicationClassesFolderPath);
    }
}
