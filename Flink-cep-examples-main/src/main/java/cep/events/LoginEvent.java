package cep.events;

public class LoginEvent implements BaseEvent {
    private long timestamp;
    private String ipAddress;
    private boolean successful;
    private String eventId;

    public LoginEvent(long timestamp, String ipAddress, boolean successful, String eventId) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.successful = successful;
        this.eventId = eventId;
    }

    @Override
    public Object getField(String fieldName) {
        switch (fieldName) {
            case "timestamp":
                return timestamp;
            case "ipAddress":
                return ipAddress;
            case "successful":
                return successful;
            case "eventId":
                return eventId;
            default:
                throw new IllegalArgumentException("Field not found: " + fieldName);
        }
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", successful=" + successful +
                ", eventId='" + eventId + '\'' +
                '}';
    }
}
