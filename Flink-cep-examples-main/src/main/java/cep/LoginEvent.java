package cep;

public class LoginEvent {
    private long timestamp;
    private String ipAddress;
    private boolean successful;

    public LoginEvent(long timestamp, String ipAddress, boolean successful) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.successful = successful;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", successful=" + successful +
                '}';
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
}