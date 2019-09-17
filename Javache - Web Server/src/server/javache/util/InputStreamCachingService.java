package server.javache.util;

import server.javache.io.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamCachingService {

    private static final int READING_KILL_PROCESS = 5000;

    private static final String CONTENT_LOADING_FAILURE_EXCEPTION_MESSAGE = "Failed loading request content.";

    private String content;

    public InputStreamCachingService() {
    }

    public InputStream getOrCacheInputStream(InputStream inputStream) throws IOException {
        if (content == null) {
            int counter = 0;

            while (counter++ < READING_KILL_PROCESS && (content == null || content.length() < 1 )) {
                content = new Reader().readAllLines(inputStream);
            }

            if (content == null || content.length() == 0) {
                this.evictCache();
                throw new IllegalArgumentException(CONTENT_LOADING_FAILURE_EXCEPTION_MESSAGE);
            }
        }

        return new ByteArrayInputStream(content.getBytes());
    }

    public void evictCache() {
        this.content = null;
    }
}