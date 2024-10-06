package cep;

public class Event {
    private String ipAddress;
    private boolean successfulLogin;
    private long timestamp;

    public Event(String ipAddress, boolean successfulLogin, long timestamp) {
        this.ipAddress = ipAddress;
        this.successfulLogin = successfulLogin;
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccessfulLogin() {
        return successfulLogin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getField(String fieldName) {
        switch (fieldName) {
            case "ip_address":
                return getIpAddress();
            case "successful_login":
                return isSuccessfulLogin();
            default:
                throw new IllegalArgumentException("Campo sconosciuto: " + fieldName);
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "ipAddress='" + ipAddress + '\'' +
                ", successfulLogin=" + successfulLogin +
                ", timestamp=" + timestamp +
                '}';
    }
}
