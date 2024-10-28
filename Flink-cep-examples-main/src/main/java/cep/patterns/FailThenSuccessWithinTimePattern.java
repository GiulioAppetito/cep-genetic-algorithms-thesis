package cep.patterns;

import cep.events.LoginEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class FailThenSuccessWithinTimePattern {

    public static Pattern<LoginEvent, ?> getPattern() {
        return Pattern.<LoginEvent>begin("fail")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return !event.getField("successful").equals("true"); // Detect a failed login
                    }
                })
                .next("success")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return event.getField("successful").equals("true"); // Detect a successful login
                    }
                })
                .within(Time.seconds(60 * 10));
    }

    public static void main(String[] args) {
        Pattern<LoginEvent, ?> pattern = getPattern();
        System.out.println("Generated Pattern: " + pattern);
    }
}
