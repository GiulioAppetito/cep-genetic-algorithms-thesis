package flinkCEP.events;

public class LoginEvent {
    private long timestamp;
    private String ipAddress;
    private boolean successfulLogin;

    public LoginEvent(long timestamp, String ipAddress, boolean successfulLogin) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.successfulLogin = successfulLogin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccessfulLogin() {
        return successfulLogin;
    }
}
