package flinkCEP.queries;

import flinkCEP.events.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.List;

public class FlinkCEPQuery {
    private int failedAttempts;
    private float timeoutSeconds;
    private AfterMatchSkipStrategy skipStrategy;

    public FlinkCEPQuery(int failedAttempts, float timeoutSeconds, AfterMatchSkipStrategy skipStrategy) {
        this.failedAttempts = failedAttempts;
        this.timeoutSeconds = timeoutSeconds;
        this.skipStrategy = skipStrategy;
    }

    // execute the query with the fixed parameters
    public DataStream<String> execute(StreamExecutionEnvironment env, DataStream<LoginEvent> loginEventStream) {
        Pattern<LoginEvent, ?> loginFailPattern = Pattern.<LoginEvent>begin("failures", skipStrategy)
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return !event.isSuccessfulLogin();
                    }
                })
                .timesOrMore(failedAttempts)
                .within(Time.seconds((long) timeoutSeconds));

        PatternStream<LoginEvent> patternStream = CEP.pattern(
                loginEventStream.keyBy(event -> event.getIpAddress()),
                loginFailPattern
        );

        return patternStream.select((PatternSelectFunction<LoginEvent, String>) pattern -> {
            List<LoginEvent> failures = pattern.get("failures");
            LoginEvent firstFail = failures.get(0);
            LoginEvent lastFail = failures.get(failures.size() - 1);

            return "Failures: " + failures.size() +
                    " | IP: " + firstFail.getIpAddress() +
                    " | Between: " + firstFail.getTimestamp() + " and " + lastFail.getTimestamp();
        });
    }
}
