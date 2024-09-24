package flinkCEP.cases;

public class LoginEvent {
    public long timestamp;
    public String ipAddress;
    public boolean successfulLogin;

    public LoginEvent(long timestamp, String ipAddress, boolean successfulLogin) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.successfulLogin = successfulLogin;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", successfulLogin=" + successfulLogin +
                '}';
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
