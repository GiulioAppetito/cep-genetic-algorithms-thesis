package cep.events;

public class Event {
    private String name;
    private int value;  // Cambia il tipo se necessario
    private long timestamp;

    // Costruttore
    public Event(String name, int value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    // Getter standard
    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Metodo per ottenere i campi in modo dinamico
    public Object getFieldValue(String fieldName) {
        switch (fieldName) {
            case "name":
                return getName();
            case "value":
                return getValue();
            case "timestamp":
                return getTimestamp();
            default:
                throw new IllegalArgumentException("Field not found: " + fieldName);
        }
    }
}
