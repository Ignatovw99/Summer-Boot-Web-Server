package server.javache.util;

public class LoggingService {

    public void info(String content) {
        System.out.println("\u001B[36m".concat("[INFO]").concat(content));
    }

    public void warning(String content) {
        System.out.println("\u001B[33m".concat("[WARNING]").concat(content));
    }

    public void error(String content) {
        System.out.println("\u001B[31m".concat("[ERROR]").concat(content));
    }
}