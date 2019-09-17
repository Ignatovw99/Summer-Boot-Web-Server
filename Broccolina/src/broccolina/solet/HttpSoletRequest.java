package broccolina.solet;

import server.javache.http.HttpRequest;

import java.io.InputStream;

public interface HttpSoletRequest extends HttpRequest {

    InputStream getInputStream();
}