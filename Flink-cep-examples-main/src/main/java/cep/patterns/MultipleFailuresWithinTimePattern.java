package cep.patterns;

import cep.events.LoginEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class MultipleFailuresWithinTimePattern {

    public static Pattern<LoginEvent, ?> getPattern() {

        int numFailures = 5;
        long timeInterval = 60;
        return Pattern.<LoginEvent>begin("fail")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return !event.getField("successful").equals("true"); // Only failed logins
                    }
                })
                .timesOrMore(numFailures) // At least numFailures failures
                .within(Time.seconds(timeInterval)); // Within the timeInterval seconds
    }
}
