package cep;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlinkCEPExample {

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final String directory = "C:\\Users\\giuli\\OneDrive\\Desktop\\Tesi\\Thesis\\Datasets\\processed-sshd-logs\\processed-sshd-logs\\";
        final String filename = "ithaca-sshd-processed-simple.csv";

        // Read the CSV file and create a DataStream
        DataStream<LoginEvent> loginEventStream = env
                .readTextFile(directory + filename)
                .map(new MapFunction<String, LoginEvent>() {
                    @Override
                    public LoginEvent map(String line) throws Exception {
                        String[] fields = line.split(",");
                        try {
                            long timestamp = Long.parseLong(fields[0]);
                            String ipAddress = fields[1];
                            boolean successful = Boolean.parseBoolean(fields[2]);
                            return new LoginEvent(timestamp, ipAddress, successful);
                        } catch (NumberFormatException e) {
                            return null; // Skip header or invalid rows
                        }
                    }
                })
                .filter(event -> event != null)  // Filter out null events
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, recordTimestamp) -> event.getTimestamp())
                );

        // Group events by IP address
        KeyedStream<LoginEvent, String> keyedStream = loginEventStream.keyBy(LoginEvent::getIpAddress);

        // Pattern: Detect multiple consecutive successful logins
        Pattern<LoginEvent, ?> multipleSuccess = Pattern.<LoginEvent>begin("success")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return event.isSuccessful(); // Only successful logins
                    }
                })
                .timesOrMore(3); // At least 3 successful logins

        // Apply the pattern to the keyed stream
        PatternStream<LoginEvent> patternStream = CEP.pattern(keyedStream, multipleSuccess);

        // Select the match and print the results
        DataStream<String> resultStream = patternStream.select(
                new PatternSelectFunction<LoginEvent, String>() {
                    @Override
                    public String select(Map<String, List<LoginEvent>> pattern) throws Exception {
                        // Retrieve successful events from the pattern
                        List<LoginEvent> successes = pattern.get("success");

                        // Create a string for the event sequence
                        String sequence = successes.stream()
                                .map(event -> String.format("IP: %s, Timestamp: %d, Successful: %b",
                                        event.getIpAddress(), event.getTimestamp(), event.isSuccessful()))
                                .collect(Collectors.joining(" -> ")); // Use "->" to show event flow

                        return "Pattern found: " + sequence;
                    }
                }
        );

        // Print the results
        resultStream.print();

        // Execute the job
        env.execute("Login Pattern Detection with Flink CEP");
    }
}
