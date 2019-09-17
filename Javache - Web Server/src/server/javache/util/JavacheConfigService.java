package server.javache.util;

import server.javache.WebConstants;
import server.javache.io.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JavacheConfigService {

    private static final String CONFIGURATION_FILE_ABSENCE_MESSAGE = "Request Handler Priority configuration file does not exist.";

    private static final String CONFIG_FOLDER_PATH = WebConstants.SERVER_ROOT_COMPILED_FOLDER_PATH.concat("config/");

    private static final String REQUEST_HANDLER_PRIORITY_CONFIG_FILE_PATH = CONFIG_FOLDER_PATH.concat("config.ini");

    private Set<String> requestHandlerPriority;

    public JavacheConfigService() {
        this.initializeConfigurations();
    }

    private void loadRequestHandlerConfig() throws IOException {
        File priorityConfigFile = new File(REQUEST_HANDLER_PRIORITY_CONFIG_FILE_PATH);

        if (!priorityConfigFile.exists() || !priorityConfigFile.isFile()) {
            throw new IllegalArgumentException(CONFIGURATION_FILE_ABSENCE_MESSAGE);
        }

        String configFileContent = new Reader()
                .readAllLines(new FileInputStream(priorityConfigFile));

        this.requestHandlerPriority =
                Arrays.stream(configFileContent
                                .replace("request-handlers: ", "")
                                .split(","))
                                .collect(Collectors.toSet());
    }

    private void initializeConfigurations() {
        try {
            this.loadRequestHandlerConfig();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Set<String> getRequestHandlerPriority() {
        return Collections.unmodifiableSet(this.requestHandlerPriority);
    }
}