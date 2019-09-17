package server.javache.http;

import java.util.Map;

public interface HttpRequest {

    Map<String, String> getHeaders();

    Map<String, String> getQueryParameters();

    Map<String, String> getBodyParameters();

    Map<String, HttpCookie> getCookies();

    String getMethod();

    String getRequestUrl();

    HttpSession getSession();

    void setMethod(String method);

    void setRequestUrl(String requestUrl);

    void setSession(HttpSession session);

    void addHeader(String header, String value);

    void addBodyParameter(String parameter, String value);

    boolean isResource();
}