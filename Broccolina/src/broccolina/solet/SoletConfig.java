package broccolina.solet;

import java.util.Map;

public interface SoletConfig {

    Map<String, Object> getAttributes();

    Object getAttribute(String name);

    void addAttribute(String name, Object attribute);

    void deleteAttribute(String name);
}