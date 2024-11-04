package events.custom;

import events.engineering.BaseEvent;

import java.util.HashMap;
import java.util.Map;

public class LoginEvent extends BaseEvent {
    private String ipAddress;
    private Boolean successfulLogin;

    public LoginEvent(Long timestamp, String ipAddress, Boolean successfulLogin) {
        super(timestamp);
        this.ipAddress = ipAddress;
        this.successfulLogin = successfulLogin;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public Boolean getSuccessfulLogin() {
        return successfulLogin;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("ip_address", ipAddress);
        map.put("successful_login", successfulLogin);
        return map;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", successfulLogin=" + successfulLogin +
                '}';
    }
}
