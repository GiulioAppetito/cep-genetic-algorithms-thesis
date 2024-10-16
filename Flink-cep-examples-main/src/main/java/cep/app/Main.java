package cep.app;

import cep.events.BaseEvent;
import cep.events.ExampleEvent;
import cep.utils.FlinkCEPPatternGenerator;
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
        // Create the Flink environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // Pattern string from the grammar
        String patternString = "begin event1 where value > 8 followedBy event2 where value == 5 followedBy event3 where value == 10\n";

        // Example datastream (just for testing)
        List<BaseEvent> events = Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 5, 5000L),
                new ExampleEvent("C", 10, 7000L),
                new ExampleEvent("D", 14, 8000L),
                new ExampleEvent("E", 5, 12000L),
                new ExampleEvent("F", 8, 13000L),
                new ExampleEvent("G",7,18000L)
        );

        // DataStream with BaseEvent type
        DataStream<BaseEvent> eventStream = env.fromCollection(events);

        // Timestamp and watermark strategy
        eventStream = eventStream.assignTimestampsAndWatermarks(
                WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> (Long) event.getFieldValue("timestamp"))
        );

        // Generate the pattern from the string
        Pattern<BaseEvent, ?> pattern = FlinkCEPPatternGenerator.generatePattern(patternString);

        // Apply the pattern to the datastream
        PatternStream<BaseEvent> patternStream = CEP.pattern(eventStream, pattern);

        // Extract the results
        DataStream<List<Tuple3<String, Integer, Long>>> resultStream = patternStream.select(new PatternSelectFunction<BaseEvent, List<Tuple3<String, Integer, Long>>>() {
            @Override
            public List<Tuple3<String, Integer, Long>> select(Map<String, List<BaseEvent>> pattern) {
                List<Tuple3<String, Integer, Long>> matchedEvents = new ArrayList<>();

                // Iterate over each entry in the pattern to collect all matching events
                for (Map.Entry<String, List<BaseEvent>> entry : pattern.entrySet()) {
                    List<BaseEvent> events = entry.getValue();

                    for (BaseEvent event : events) {
                        matchedEvents.add(new Tuple3<>(
                                (String) event.getFieldValue("name"),
                                (Integer) event.getFieldValue("value"),
                                (Long) event.getFieldValue("timestamp")
                        ));
                    }
                }

                return matchedEvents;
            }
        });

        // Sink to print the results
        resultStream.addSink(new SinkFunction<List<Tuple3<String, Integer, Long>>>() {
            @Override
            public void invoke(List<Tuple3<String, Integer, Long>> value, Context context) {
                // Print the complete sequence of matching events
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
