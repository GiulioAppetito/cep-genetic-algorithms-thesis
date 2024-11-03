package events.source;

import events.BaseEvent;

import java.util.Map;

public class GenericEvent extends BaseEvent {

    public GenericEvent(Long timestamp) {
        super(timestamp);
    }

    @Override
    public Map<String, Object> toMap() {
        return getBaseAttributesMap();
    }
}
