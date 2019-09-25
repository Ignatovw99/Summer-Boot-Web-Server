package broccolina.util;

import server.javache.http.*;

import java.util.Date;
import java.util.Map;

public class SessionManagementService {

    private static final String SESSION_KEY = "Javache";

    private HttpSessionStorage sessionStorage;

    public SessionManagementService() {
        this.sessionStorage = new HttpSessionStorageImpl();
    }

    public void initSessionIfExistent(HttpRequest request) {
        if (request.getCookies().containsKey(SESSION_KEY)) {
            HttpCookie sessionCookie = request.getCookies().get(SESSION_KEY);

            String sessionId = sessionCookie.getValue();
            HttpSession session = this.sessionStorage.getById(sessionId);

            if (session != null && session.isValid()) {
                request.setSession(session);
            } else {
                System.out.println("There is an invalid cookie");
                //request.getCookies().remove(SESSION_KEY);
            }
        }
    }
    
    public void sendSessionIfExistent(HttpRequest request, HttpResponse response) {
        if (request.getSession() != null) {
            if (this.sessionStorage.getById(request.getSession().getId()) == null) {
                this.sessionStorage.addSession(request.getSession());
                response.addCookie(SESSION_KEY, request.getSession().getId());
            }

            if (!request.getSession().isValid()){
                response.addCookie(SESSION_KEY, "removed; expires=".concat(new Date(0).toString()));
            }
        } else {
            HttpCookie cookie = request.getCookies().get(SESSION_KEY);
            if (cookie != null
                    && this.sessionStorage.getById(cookie.getValue()) == null) {
                response.addCookie(SESSION_KEY, "removed; expires=".concat(new Date(0).toString()));
            }
        }
    }

    public void clearInvalidSessions() {
        this.sessionStorage.refreshSessions();
    }
}
