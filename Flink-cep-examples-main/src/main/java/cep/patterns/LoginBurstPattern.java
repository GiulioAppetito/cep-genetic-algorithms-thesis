package cep.patterns;

import cep.events.LoginEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class LoginBurstPattern {

    public static Pattern<LoginEvent, ?> getPattern(int numAttempts, int timeInterval) {
        return Pattern.<LoginEvent>begin("login")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return true; // Match both successful and failed logins
                    }
                })
                .timesOrMore(numAttempts) // Detect numAttempts (success or failure)
                .within(Time.seconds(timeInterval)); // All within timeInterval seconds
    }
}
