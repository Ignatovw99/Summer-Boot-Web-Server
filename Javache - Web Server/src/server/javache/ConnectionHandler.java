package server.javache;

import server.javache.api.RequestHandler;
import server.javache.util.InputStreamCachingService;
import server.javache.util.LoggingService;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ConnectionHandler extends Thread {

    private Socket clientSocket;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private Set<RequestHandler> requestHandlers;

    private InputStreamCachingService cachingService;

    private LoggingService loggingService;

    public ConnectionHandler(Socket clientSocket, Set<RequestHandler> requestHandlers, InputStreamCachingService cachingService) {
        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
        this.cachingService = cachingService;
        this.loggingService = new LoggingService();
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            this.loggingService.error(e.getMessage());
        }
    }

    private void processClientConnection() throws IOException {
        for (RequestHandler requestHandler : this.requestHandlers) {
            requestHandler.handleRequest(
                    this.cachingService.getOrCacheInputStream(this.clientSocketInputStream),
                    this.clientSocketOutputStream
            );

            if(requestHandler.hasIntercepted()) break;
        }
    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
            this.cachingService.evictCache();
        } catch (IOException e) {
            this.loggingService.error(e.getMessage());
        }
    }
}