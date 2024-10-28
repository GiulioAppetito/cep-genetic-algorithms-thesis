package cep.events;

public class SimpleEvent implements BaseEvent{
    private final String type;
    private final float value;

    public SimpleEvent(String type, float value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SimpleEvent{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public Object getField(String fieldName) {
        switch (fieldName){
            case "type":
                return type;
            case "value":
                return value;
            default:
                return null;
        }
    }
}
