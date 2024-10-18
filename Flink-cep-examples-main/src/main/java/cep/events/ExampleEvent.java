package cep.events;

public class ExampleEvent implements BaseEvent {
    private String name;
    private int value;
    private long timestamp;

    public ExampleEvent(String name, int value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        switch (fieldName) {
            case "name":
                return name;
            case "value":
                return value;
            case "timestamp":
                return timestamp;
            default:
                throw new IllegalArgumentException("Field not found: " + fieldName);
        }
    }
}
