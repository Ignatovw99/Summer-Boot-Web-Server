package broccolina.solet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SoletConfigImpl implements SoletConfig {

    private Map<String, Object> attributes;

    public SoletConfigImpl() {
        this.attributes = new HashMap<>();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    @Override
    public Object getAttribute(String name) {
        if (!this.attributes.containsKey(name)) {
            return null;
        }

        return this.attributes.get(name);
    }

    @Override
    public void addAttribute(String name, Object attribute) {
        this.attributes.put(name, attribute);
    }

    @Override
    public void deleteAttribute(String name) {
        this.attributes.remove(name);
    }
}