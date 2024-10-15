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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // Example datastream (just for testing)
        List<Event> events = Arrays.asList(
                new Event("A", 6, 1000L),    // Soddisfa la condizione "start where value > 5"
                new Event("B", 8, 4000L),    // Soddisfa la condizione "middle where value < 10"
                new Event("C", 12, 6000L),   // NON soddisfa la condizione (valore > 10), quindi ignora
                new Event("D", 9, 12000L),   // NON soddisfa la condizione (value != 3)
                new Event("E", 3, 16000L)    // Soddisfa la condizione "end where value == 3", ma troppo tardi (dopo 15s)
        );

        DataStream<Event> eventStream = env.fromCollection(events);

        // Timestamp and watermark strategy
        eventStream = eventStream.assignTimestampsAndWatermarks(
                WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
        );

        // Pattern string from the grammar
        String patternString = "start where value==8";

        // The visitor generates the pattern starting from the string
        Pattern<Event, ?> pattern = FlinkCEPPatternGenerator.generatePattern(patternString);

        // Apply the pattern to the datastream
        PatternStream<Event> patternStream = CEP.pattern(eventStream, pattern);

        // Extract results
        DataStream<Tuple3<String, Integer, Long>> resultStream = patternStream.select(new PatternSelectFunction<Event, Tuple3<String, Integer, Long>>() {
            @Override
            public Tuple3<String, Integer, Long> select(Map<String, List<Event>> pattern) {
                // Restituisce solo il primo evento corrispondente
                Event firstEvent = pattern.values().iterator().next().get(0);
                return new Tuple3<>(firstEvent.getName(), firstEvent.getValue(), firstEvent.getTimestamp());
            }
        });

        // Sink to print the results
        resultStream.addSink(new SinkFunction<Tuple3<String, Integer, Long>>() {
            @Override
            public void invoke(Tuple3<String, Integer, Long> value, Context context) {
                // Stampa solo i risultati dei match
                System.out.println("Pattern match result: " + value);
            }
        });

        // Avvio dell'esecuzione Flink
        env.execute("Flink CEP Pattern Example");
    }
}
