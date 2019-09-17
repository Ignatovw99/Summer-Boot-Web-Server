package broccolina.solet;

import server.javache.http.HttpResponse;

import java.io.OutputStream;

public interface HttpSoletResponse extends HttpResponse {

    OutputStream getOutputStream();
}