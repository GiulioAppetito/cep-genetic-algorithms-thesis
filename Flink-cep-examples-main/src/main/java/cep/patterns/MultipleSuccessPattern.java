package cep.patterns;

import cep.events.LoginEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class MultipleSuccessPattern {

    public static Pattern<LoginEvent, ?> getPattern() {
        return Pattern.<LoginEvent>begin("success")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return event.isSuccessful(); // Only successful logins
                    }
                })
                .timesOrMore(3); // At least 3 successful login
    }
}
