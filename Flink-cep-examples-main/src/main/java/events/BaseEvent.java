package events;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseEvent {
    protected Long timestamp;
    protected Map<String, Object> attributes;

    public BaseEvent(Long timestamp) {
        this.timestamp = timestamp;
        this.attributes = new HashMap<>();
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public abstract Map<String, Object> toMap();

    protected Map<String, Object> getBaseAttributesMap() {
        Map<String, Object> result = new HashMap<>(attributes);
        result.put("timestamp", timestamp);
        return result;
    }
}
