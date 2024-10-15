package cep.app;

import cep.events.Event;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // Pattern string from the grammar
        String patternString = "start where value==9";

        // Example datastream (just for testing)
        List<Event> events = Arrays.asList(
                new Event("A", 9, 1000L),
                new Event("B", 8, 4000L),
                new Event("C", 12, 6000L),
                new Event("D", 9, 12000L),
                new Event("E", 3, 16000L)
        );

        DataStream<Event> eventStream = env.fromCollection(events);

        // Timestamp and watermark strategy
        eventStream = eventStream.assignTimestampsAndWatermarks(
                WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
        );

        // The visitor generates the pattern starting from the string
        Pattern<Event, ?> pattern = FlinkCEPPatternGenerator.generatePattern(patternString);

        // Apply the pattern to the datastream
        PatternStream<Event> patternStream = CEP.pattern(eventStream, pattern);

        // Extract results
        DataStream<List<Tuple3<String, Integer, Long>>> resultStream = patternStream.select(new PatternSelectFunction<Event, List<Tuple3<String, Integer, Long>>>() {
            @Override
            public List<Tuple3<String, Integer, Long>> select(Map<String, List<Event>> pattern) {
                List<Tuple3<String, Integer, Long>> matchedEvents = new ArrayList<>();

                // Iterate over each entry in the pattern to collect all matching events
                for (Map.Entry<String, List<Event>> entry : pattern.entrySet()) {
                    List<Event> events = entry.getValue();

                    for (Event event : events) {
                        matchedEvents.add(new Tuple3<>(event.getName(), event.getValue(), event.getTimestamp()));
                    }
                }

                return matchedEvents;
            }
        });

        // Sink to print the results
        resultStream.addSink(new SinkFunction<List<Tuple3<String, Integer, Long>>>() {
            @Override
            public void invoke(List<Tuple3<String, Integer, Long>> value, Context context) {
                // Print the entire sequence of matching events
                System.out.println("Pattern match sequence:");
                for (Tuple3<String, Integer, Long> event : value) {
                    System.out.println(event);
                }
            }
        });

        // Start Flink execution
        env.execute("Flink CEP Pattern Example");
    }
}
