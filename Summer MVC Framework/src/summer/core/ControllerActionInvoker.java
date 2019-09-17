package summer.core;

import broccolina.solet.HttpSoletRequest;
import broccolina.solet.HttpSoletResponse;
import server.javache.http.HttpSession;
import summer.api.Model;
import summer.util.ControllerActionPair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class ControllerActionInvoker {

    private DependencyContainer dependencyContainer;

    public ControllerActionInvoker(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    private boolean isPrimitive(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(int.class.getSimpleName())
                || parameterTypeName.equals(double.class.getSimpleName())
                || parameterTypeName.equals(boolean.class.getSimpleName())
                || parameterTypeName.equals(long.class.getSimpleName());
    }

    private boolean isString(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        return parameter
                .getType()
                .getSimpleName()
                .equals(String.class.getSimpleName());
    }

    private boolean isWrapper(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(Integer.class.getSimpleName())
                || parameterTypeName.equals(Double.class.getSimpleName())
                || parameterTypeName.equals(Boolean.class.getSimpleName())
                || parameterTypeName.equals(Long.class.getSimpleName());
    }

    private boolean isHttpSoletRequest(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(HttpSoletRequest.class.getSimpleName());
    }

    private boolean isHttpSoletResponse(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(HttpSoletResponse.class.getSimpleName());
    }

    private boolean isHttpSession(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(HttpSession.class.getSimpleName());
    }

    private boolean isModel(Parameter parameter) {
        if (parameter == null) {
            return false;
        }

        String parameterTypeName = parameter.getType().getSimpleName();

        return parameterTypeName.equals(Model.class.getSimpleName());
    }

    private boolean isBindingModel(Parameter parameter) {
        return parameter != null
                && !this.isPrimitive(parameter)
                && !this.isString(parameter)
                && !this.isWrapper(parameter)
                && !this.isHttpSoletRequest(parameter)
                && !this.isHttpSoletResponse(parameter)
                && !this.isHttpSession(parameter)
                && !this.isModel(parameter);
    }

    private boolean isPathVariable(Parameter parameter) {
        return this.isPrimitive(parameter)
                || this.isWrapper(parameter)
                || this.isString(parameter);
    }

    private Object instantiateBindingModel(Parameter currentParameter) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return currentParameter
                .getType()
                .getDeclaredConstructor()
                .newInstance();
    }

    private Object parseValue(Class<?> fieldType, String value) {
        if (fieldType.getSimpleName().equals(int.class.getSimpleName())
                || fieldType.getSimpleName().equals(Integer.class.getSimpleName())) {
            return Integer.valueOf(value);
        } else if (fieldType.getSimpleName().equals(long.class.getSimpleName())
                || fieldType.getSimpleName().equals(Long.class.getSimpleName())) {
            return Long.valueOf(value);
        } else if (fieldType.getSimpleName().equals(double.class.getSimpleName())
                || fieldType.getSimpleName().equals(Double.class.getSimpleName())) {
            return Double.valueOf(value);
        } else if (fieldType.getSimpleName().equals(boolean.class.getSimpleName())
                || fieldType.getSimpleName().equals(Boolean.class.getSimpleName())) {
            return Boolean.valueOf(value);
        }

        return value;
    }

    private void populateBindingModel(Object bindingModel, HttpSoletRequest request) {
        try {
            Arrays.stream(bindingModel.getClass()
                    .getDeclaredFields())
                    .forEach(field -> {
                        field.setAccessible(true);

                        if (request.getBodyParameters().containsKey(field.getName())) {
                            try {
                                String parameterValue = URLDecoder.decode(request.getBodyParameters().get(field.getName()), StandardCharsets.UTF_8);

                                Object parsedParameterValue = this.parseValue(field.getType(), parameterValue);
                                field.set(bindingModel, parsedParameterValue);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ignored) {
            ;
        }
    }

    private Object[] getActionArguments(Method action, Set<Object> parameters) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Parameter[] actionParameters = action.getParameters();
        Iterator<Object> parametersIterator = parameters.iterator();

        Object[] actionArguments = new Object[actionParameters.length];

        for (int i = 0; i < actionParameters.length; i++) {
            Parameter currentParameter = actionParameters[i];

            System.out.println(currentParameter);

            if (this.isPathVariable(currentParameter) && parametersIterator.hasNext()) {
                Object pathVariable = parametersIterator.next();
              actionArguments[i] = this.parseValue(currentParameter.getType(), pathVariable.toString());
            } else if (this.isModel(currentParameter)) {
                actionArguments[i] = this.dependencyContainer.getObject(Model.class.getSimpleName());
            } else if (this.isHttpSoletRequest(currentParameter)) {
                actionArguments[i] = this.dependencyContainer.getObject(HttpSoletRequest.class.getSimpleName());
            } else if (this.isHttpSoletResponse(currentParameter)) {
                actionArguments[i] = this.dependencyContainer.getObject(HttpSoletResponse.class.getSimpleName());
            } else if (this.isHttpSession(currentParameter)) {
                actionArguments[i] = this.dependencyContainer.getObject(HttpSession.class.getSimpleName());
            } else if (this.isModel(currentParameter)) {

            } else if (this.isBindingModel(currentParameter)) {
                Object bindingModel = this.instantiateBindingModel(currentParameter);

                HttpSoletRequest request = (HttpSoletRequest) this.dependencyContainer
                        .getObject(HttpSoletRequest.class.getSimpleName());
                this.populateBindingModel(bindingModel, request);

                actionArguments[i] = bindingModel;
            }
        }

        return actionArguments;
    }

    public Object invokeAction(ControllerActionPair controllerActionPair) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Object[] actionArguments = this.getActionArguments(controllerActionPair.getAction(), controllerActionPair.getParameters());

        Object actionResult;

        if (actionArguments.length > 0) {
            actionResult = controllerActionPair.getAction()
                    .invoke(controllerActionPair.getController(), actionArguments);
        } else {
            actionResult = controllerActionPair.getAction()
                    .invoke(controllerActionPair.getController());
        }

        controllerActionPair.clearParameters();
        return actionResult;
    }
}
