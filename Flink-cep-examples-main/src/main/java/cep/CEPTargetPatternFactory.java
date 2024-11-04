package cep;

import events.engineering.BaseEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

import java.util.ArrayList;
import java.util.List;

public class CEPTargetPatternFactory {

    public static List<Pattern<BaseEvent, ?>> createReferencePatterns() {
        List<Pattern<BaseEvent, ?>> targetPatterns = new ArrayList<>();

        Pattern<BaseEvent, BaseEvent> pattern1 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object ipAddress = event.toMap().get("ip_address");
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern1);

        Pattern<BaseEvent, BaseEvent> pattern2 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object ipAddress = event.toMap().get("ip_address");
                        Object successfulLogin = event.toMap().get("successful_login");
                        return "129.16.0.5".equals(ipAddress) &&
                                successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .next("event2")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object ipAddress = event.toMap().get("ip_address");
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern2);

        Pattern<BaseEvent, BaseEvent> pattern3 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .followedBy("event2")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .followedBy("event3")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern3);

        return targetPatterns;
    }
}
