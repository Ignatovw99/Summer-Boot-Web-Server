package server.javache;

import server.javache.util.InputStreamCachingService;
import server.javache.util.JavacheConfigService;
import server.javache.util.LoggingService;
import server.javache.util.RequestHandlerLoadingService;

import java.io.*;
import java.net.*;
import java.util.concurrent.FutureTask;

public class Server {

    private static final String LISTENING_MESSAGE = "Listening on port: ";

    private static final String TIMEOUT_DETECTION_MESSAGE = "Timeout detected!";

    private static final Integer SOCKET_TIMEOUT_MILLISECONDS = 5000;

    private int port;

    private int timeouts;

    private ServerSocket server;

    private LoggingService loggingService;

    private JavacheConfigService javacheConfigService;

    private RequestHandlerLoadingService requestHandlerLoadingService;

    private InputStreamCachingService cachingService;

    public Server(int port) {
        this.port = port;
        this.timeouts = 0;
        this.loggingService = new LoggingService();
        this.javacheConfigService = new JavacheConfigService();
        this.requestHandlerLoadingService = new RequestHandlerLoadingService();
        this.cachingService = new InputStreamCachingService();
        this.initRequestHandlers();
    }

    private void initRequestHandlers() {
        try {
            this.requestHandlerLoadingService.loadRequestHandler(
                    this.javacheConfigService.getRequestHandlerPriority()
            );
        } catch (Exception e) {
            this.loggingService.error(e.getMessage());
        }
    }

    public void run() throws IOException {
        this.server = new ServerSocket(this.port);
        this.loggingService.info(LISTENING_MESSAGE + this.port);

        this.server.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

        while(true) {
            try(Socket clientSocket = this.server.accept()) {
                clientSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

                ConnectionHandler connectionHandler
                        = new ConnectionHandler(clientSocket, this.requestHandlerLoadingService.getRequestHandlers(), this.cachingService);

                FutureTask<?> task = new FutureTask<>(connectionHandler, null);
                task.run();
            } catch(SocketTimeoutException e) {
//                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
//                    this.loggingService.error(stackTraceElement.toString());
//                }
                this.loggingService.warning(TIMEOUT_DETECTION_MESSAGE);
                this.timeouts++;
            }
        }
    }
}