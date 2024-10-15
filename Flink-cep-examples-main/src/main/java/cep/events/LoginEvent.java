package cep.events;

public class LoginEvent {
    private long timestamp;
    private String ipAddress;
    private boolean successful;
    private String eventId; // Nuovo campo per identificare univocamente l'evento

    public LoginEvent(long timestamp, String ipAddress, boolean successful, String eventId) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.successful = successful;
        this.eventId = eventId; // Inizializza il nuovo campo
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getEventId() {
        return eventId;
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
