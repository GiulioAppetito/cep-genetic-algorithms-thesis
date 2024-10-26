package cep.patterns;

import cep.events.LoginEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class MultipleFailuresFollowedBySuccessPattern {

    public static Pattern<LoginEvent, ?> getPattern() {
        return Pattern.<LoginEvent>begin("fail")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return !event.getField("successful").equals("true");
                    }
                })
                .timesOrMore(5)
                .next("success")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return event.getField("successful").equals("true");
                    }
                });
    }
}
