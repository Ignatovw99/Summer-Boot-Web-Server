package summer.core;

import broccolina.solet.*;
import server.javache.http.HttpSession;
import server.javache.http.HttpStatus;
import summer.api.Model;
import summer.api.PathVariable;
import summer.util.ControllerActionPair;
import summer.util.ControllerLoadingService;
import summer.util.TemplateEngine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebSolet(route = "/*")
public class DispatcherSolet extends BaseHttpSolet {

    private String applicationClassesFolderPath;

    private DependencyContainer dependencyContainer;

    private ControllerLoadingService controllerLoadingService;

    private TemplateEngine templateEngine;

    private ControllerActionInvoker controllerActionInvoker;

    private ControllerActionPair getControllerActionPairCandidate(HttpSoletRequest request) {
        Set<Object> actionParameters = new LinkedHashSet<>();

        ControllerActionPair controllerActionPairCandidate = this.controllerLoadingService.getLoadedControllersAndActions()
                .get(request.getMethod())
                .entrySet()
                .stream()
                .filter(action -> {
                    Pattern routePattern = Pattern.compile("^".concat(action.getKey()).concat("$"));
                    Matcher routeMatcher = routePattern.matcher(request.getRequestUrl());

                    if (routeMatcher.find()) {
                        System.out.println("The current action route matches the request url.");
                        List<Parameter> pathVariables = Arrays.stream(action.getValue().getAction().getParameters())
                                .filter(parameter -> parameter.isAnnotationPresent(PathVariable.class))
                                .collect(Collectors.toList());

                        for (Parameter pathVariable : pathVariables) {
                            String variableName = pathVariable.getAnnotation(PathVariable.class)
                                    .name();

                            String variableValue = routeMatcher.group(variableName);
                            actionParameters.add(variableValue);
                        }
                        
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        for (Object actionParameter : actionParameters) {
            controllerActionPairCandidate.addParameter(actionParameter);
        }

        return controllerActionPairCandidate;
    }

    private void handleRequest(HttpSoletRequest request, HttpSoletResponse response) {
        ControllerActionPair controllerActionPair = this.getControllerActionPairCandidate(request);

        if (controllerActionPair == null) {
            System.out.println("Controller Action Pair is Null");
            if (request.getMethod().equals("GET")) {
                super.doGet(request, response);
            } else if (request.getMethod().equals("POST")) {
                super.doPost(request, response);
            }

            return;
        }

        try {
            String result = this.controllerActionInvoker
                    .invokeAction(controllerActionPair)
                    .toString();

            System.out.println("Action Result: " + result);

            response.setStatusCode(HttpStatus.OK);

            if (result.startsWith("template:")) {
                String templateName = result.split(":")[1];
                response.addHeader("Content-Type", "text/html");
                response.setContent(
                        this.templateEngine.loadTemplate(
                                templateName,
                                (Model) this.dependencyContainer.getObject(Model.class.getSimpleName())
                        ).getBytes()
                );
            } else if (result.startsWith("redirect:")) {
                String route = result.split(":")[1];

                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.addHeader("Location", route);
            } else  {
                response.addHeader("Content-Type", "text/plain");
                response.setContent(result.getBytes());
            }
        } catch (IllegalAccessException | InvocationTargetException | IOException | InstantiationException | NoSuchMethodException e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

            response.addHeader("Content-Type", "text/html");

            StringBuilder content = new StringBuilder(String.format("<h1>%s</h1><p>", e.getMessage()));

            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                content.append(stackTraceElement.toString().concat("<br/>"));
            }
            content.append("</p>");

            response.setContent(content.toString().getBytes());
        }
    }

    @Override
    public void init(SoletConfig soletConfig) {
        super.init(soletConfig);

        this.applicationClassesFolderPath = soletConfig.getAttribute("application-folder").toString()
                .concat("classes")
                .concat(File.separator);

        this.dependencyContainer = new DependencyContainer();
        this.controllerLoadingService = new ControllerLoadingService();
        this.templateEngine = new TemplateEngine(
                this.getSoletConfig().getAttribute("application-folder").toString()
                        .concat("resources")
                        .concat(File.separator)
                        .concat("templates")
                        .concat(File.separator)
        );
        this.controllerActionInvoker = new ControllerActionInvoker(this.dependencyContainer);

        try {
            this.controllerLoadingService.loadControllerActionHandlers(this.applicationClassesFolderPath);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void service(HttpSoletRequest request, HttpSoletResponse response) {
        this.dependencyContainer.addInstantiatedObject(HttpSoletRequest.class.getSimpleName(), request);
        this.dependencyContainer.addInstantiatedObject(HttpSoletResponse.class.getSimpleName(), response);
        if (request.getSession() != null) {
            this.dependencyContainer.addInstantiatedObject(HttpSession.class.getSimpleName(), request.getSession());
        }

        super.service(request, response);

        this.dependencyContainer.evictCachedStaticStates();
    }

    @Override
    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) {
        this.handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) {
        this.handleRequest(request, response);
    }
}
