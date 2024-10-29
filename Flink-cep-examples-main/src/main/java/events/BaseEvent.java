package events;

import java.util.Map;

public abstract class BaseEvent {
    protected Long timestamp;

    public BaseEvent(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public abstract Map<String, Object> toMap();
}
