package events.custom;

import events.engineering.BaseEvent;

import java.util.HashMap;
import java.util.Map;

public class ExampleEvent extends BaseEvent {
    private Float v1, v2, v3, v4, v5;

    public ExampleEvent(Long timestamp, Float v1, Float v2, Float v3, Float v4, Float v5) {
        super(timestamp);
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("v1", v1);
        map.put("v2", v2);
        map.put("v3", v3);
        map.put("v4", v4);
        map.put("v5", v5);
        return map;
    }
}