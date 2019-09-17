package broccolina.solet;

import server.javache.http.HttpStatus;
import server.javache.io.Writer;

import java.io.IOException;

public abstract class BaseHttpSolet implements HttpSolet {

    private static final String GENERIC_ERROR_MESSAGE =
            "<h1>[ERROR] %s %s</h1> <h3>[MESSAGE] The page or functionality you are looking for is not found</h3>";

    private boolean isInitialized;

    private SoletConfig soletConfig;

    protected BaseHttpSolet() {
        this.isInitialized = false;
    }

    private void configureNotFound(HttpSoletRequest request, HttpSoletResponse response, String httpMethod) {
        response.setStatusCode(HttpStatus.NOT_FOUND);

        response.addHeader("Content-Type", "text/html");

        response.setContent((String.format(GENERIC_ERROR_MESSAGE, request.getRequestUrl(), httpMethod)).getBytes());
    }

    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response, "GET");
    }

    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response, "POST");
    }

    protected void doPut(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response, "PUT");
    }

    protected void doDelete(HttpSoletRequest request, HttpSoletResponse response) {
        this.configureNotFound(request, response, "DELETE");
    }

    @Override
    public void init(SoletConfig soletConfig) {
        this.isInitialized = true;
        this.soletConfig = soletConfig;
    }

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    @Override
    public SoletConfig getSoletConfig() {
        return this.soletConfig;
    }

    @Override
    public void service(HttpSoletRequest request, HttpSoletResponse response) {
        if (request.getMethod().equals("GET")) {
            this.doGet(request, response);
        } else if (request.getMethod().equals("POST")) {
            this.doPost(request, response);
        } else if (request.getMethod().equals("PUT")) {
            this.doPut(request, response);
        } else if (request.getMethod().equals("DELETE")) {
            this.doDelete(request, response);
        }
    }
}