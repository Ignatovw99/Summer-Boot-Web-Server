package toyote;

import server.javache.api.RequestHandler;
import server.javache.http.*;
import server.javache.io.Reader;
import server.javache.io.Writer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceHandler implements RequestHandler {

    private static final String APPLICATION_RESOURCES_FOLDER_NAME = "resources";

    private static final String RESOURCE_NOT_FOUND_MESSAGE =
            "<h1 style=\"text-align: center;\">The resource \"%s\" - you are looking for cannot be found.</h1>";

    private final String serverRootFolderPath;

    private boolean hasIntercepted;

    public ResourceHandler(String serverRootFolderPath) {
        this.serverRootFolderPath = serverRootFolderPath;
        this.hasIntercepted = false;
    }

    //TODO: FIX THE BUG WITH GETTING AN APPLICATION NAME
    //TODO: THE METHOD SHOULD DEPEND ON THE CURRENT LOADED APPLICATION, NOT ON THE REQUEST URL
    private String getApplicationName(String requestUrl) {
//        requestUrl = requestUrl.substring(1);
//
//        if (requestUrl.contains("/")) {
//            return requestUrl.substring(0, requestUrl.lastIndexOf("/"));
//        }

        return "ROOT";
    }

    private String getResourceName(String requestUrl) {
        return requestUrl.substring(requestUrl.lastIndexOf("/") + 1);
    }

    private void notFound(String resourceName, HttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.addHeader("Content-Type", "text/html");
        response.setContent(String.format(RESOURCE_NOT_FOUND_MESSAGE, resourceName).getBytes());
    }

    private void handleResourceRequest(String resourcesFolder, String resourceName, HttpResponse response) {
        try {
            Path resourcePath = Paths.get(
                    new URL("file:/"
                            .concat(new File(resourcesFolder.concat(File.separator).concat(resourceName)).getCanonicalPath()).replace(" ", "%20"))
                            .toURI()
            );

            byte[] resourceContent = Files.readAllBytes(resourcePath);

            response.setStatusCode(HttpStatus.OK);

            response.addHeader("Content-Type", Files.probeContentType(resourcePath));
            response.addHeader("Content-Length", String.valueOf(resourceContent.length));
            response.addHeader("Content-Disposition", "inline");

            response.setContent(resourceContent);
        } catch (URISyntaxException | IOException e) {
            this.notFound(resourceName, response);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(new Reader().readAllLines(inputStream));
            HttpResponse response = new HttpResponseImpl();

            String resourcesFolder = this.serverRootFolderPath
                    .concat("webapps")
                    .concat(File.separator)
                    .concat(this.getApplicationName(request.getRequestUrl()))
                    .concat(File.separator)
                    .concat(APPLICATION_RESOURCES_FOLDER_NAME);

            String resourceName = this.getResourceName(request.getRequestUrl());

            this.handleResourceRequest(resourcesFolder, resourceName, response);

            new Writer().writeBytes(response.getBytes(), outputStream);
            this.hasIntercepted = true;
        } catch (IOException e) {
            e.printStackTrace();
            this.hasIntercepted = false;
        }
    }

    @Override
    public boolean hasIntercepted() {
        return this.hasIntercepted;
    }
}