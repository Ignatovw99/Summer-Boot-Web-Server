package broccolina;

import broccolina.solet.*;
import broccolina.util.ApplicationLoadingService;
import broccolina.util.SessionManagementService;
import server.javache.api.RequestHandler;
import server.javache.io.Reader;
import server.javache.io.Writer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoletDispatcher implements RequestHandler {

    private final String serverRootFolderPath;

    private boolean hasIntercepted;

    private Map<String, HttpSolet> soletMap;

    private ApplicationLoadingService applicationLoadingService;

    private SessionManagementService sessionManagementService;

    public SoletDispatcher(String serverRootFolderPath) {
        System.out.println("Server folder: ".concat(serverRootFolderPath));
        this.serverRootFolderPath = serverRootFolderPath;
        this.hasIntercepted = false;
        this.applicationLoadingService = new ApplicationLoadingService();
        this.sessionManagementService = new SessionManagementService();
        this.initializeSoletMap();
    }

    private void initializeSoletMap() {
        try {
            this.soletMap = this.applicationLoadingService.loadApplications(
                    this.serverRootFolderPath
                            .concat("webapps")
                            .concat(File.separator)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpSolet findSoletCandidate(String requestUrl) {
        Pattern applicationRouteMatchPattern = Pattern.compile("\\/[a-zA-Z0-9]+\\/");
        Matcher applicationRouteMatcher = applicationRouteMatchPattern.matcher(requestUrl);

        HttpSolet soletObject = null;

        System.out.println("Request Handler request URL: " + requestUrl);

        if (this.soletMap.containsKey(requestUrl)) {
            soletObject = this.soletMap.get(requestUrl);
        }

        if (applicationRouteMatcher.find() && soletObject == null) {
            String applicationRoute = applicationRouteMatcher.group(0).concat("*");

            if (this.soletMap.containsKey(applicationRoute)) {
                soletObject = this.soletMap.get(applicationRoute);
            }
        }

        if (this.soletMap.containsKey("/*") && soletObject == null) {
            soletObject = this.soletMap.get("/*");
        }

        return soletObject;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            HttpSoletRequest request = new HttpSoletRequestImpl(
                    new Reader().readAllLines(inputStream),
                    inputStream
            );
            HttpSoletResponse response = new HttpSoletResponseImpl(outputStream);

            String requestUrl = request.getRequestUrl();

            HttpSolet soletObject = this.findSoletCandidate(requestUrl);

            if (request.isResource() || soletObject == null) {
                this.hasIntercepted = false;
                return;
            }

            this.sessionManagementService.initSessionIfExistent(request);

            Class[] soletServiceMethodParameters = Arrays.stream(soletObject.getClass().getMethods())
                    .filter(method -> method.getName().equals("service"))
                    .findFirst()
                    .orElse(null)
                    .getParameterTypes();

            soletObject.getClass()
                    .getMethod("service", soletServiceMethodParameters[0], soletServiceMethodParameters[1])
                    .invoke(soletObject, request, response);

            this.sessionManagementService.sendSessionIfExistent(request, response);

            this.sessionManagementService.clearInvalidSessions();

            new Writer().writeBytes(response.getBytes(), response.getOutputStream());

            this.hasIntercepted = true;
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            this.hasIntercepted = false;
        }
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }
}