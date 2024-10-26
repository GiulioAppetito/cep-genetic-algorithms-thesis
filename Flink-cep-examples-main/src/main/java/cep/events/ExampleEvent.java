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
    public Object getField(String fieldName) {
        return null;
    }
}
