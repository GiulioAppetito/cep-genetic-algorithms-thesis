package cep;

import cep.events.LoginEvent;
import cep.utils.PatternFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        // Environment and file path
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final String directory = "C:\\Users\\giuli\\OneDrive\\Desktop\\Tesi\\Thesis\\Datasets\\processed-sshd-logs\\processed-sshd-logs\\";
        final String filename = "ithaca-sshd-processed-simple.csv";

        // Pattern type selection from factory
        PatternFactory.PatternType selectedPatternType = PatternFactory.PatternType.MULTIPLE_FAILURES_FOLLOWED_BY_SUCCESS_PATTERN;

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

        // Choose a pattern using the PatternFactory
        Pattern<LoginEvent, ?> chosenPattern = PatternFactory.getPattern(selectedPatternType);

        // Apply the pattern to keyed stream
        PatternStream<LoginEvent> patternStream = CEP.pattern(keyedStream, chosenPattern);

        // Select the match and print results
        DataStream<String> resultStream = patternStream.select(
                new PatternSelectFunction<LoginEvent, String>() {
                    @Override
                    public String select(Map<String, List<LoginEvent>> pattern) throws Exception {
                        // Get the sequence of events from the pattern
                        List<LoginEvent> allEvents = new ArrayList<>();

                        // Add fail events if they exist
                        List<LoginEvent> failEvents = pattern.get("fail");
                        if (failEvents != null) {
                            allEvents.addAll(failEvents);
                        }

                        // Add success events if they exist
                        List<LoginEvent> successEvents = pattern.get("success");
                        if (successEvents != null) {
                            allEvents.addAll(successEvents);
                        }

                        // Check if allEvents is not empty before processing
                        if (!allEvents.isEmpty()) {
                            // Create a string for the full event sequence
                            String sequence = allEvents.stream()
                                    .map(event -> String.format("IP: %s, Timestamp: %d, Successful: %b",
                                            event.getIpAddress(), event.getTimestamp(), event.isSuccessful()))
                                    .collect(Collectors.joining(" -> ")); // Use "->" to show event flow

                            return "Pattern found: " + sequence;
                        }

                        // If no events were found, return a default message
                        return "No matching events found";
                    }
                }
        );

        // Print results
        resultStream.print();

        // Execute job
        env.execute("Login Pattern Detection with Flink CEP");
    }
}
