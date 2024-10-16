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
        // Creazione dell'ambiente Flink
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // Pattern string from the grammar
        String patternString = "begin event1 where value == 5";

        // Example datastream (just for testing)
        List<BaseEvent> events = Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 5, 5000L),
                new ExampleEvent("C", 10, 7000L),
                new ExampleEvent("D", 5, 8000L),
                new ExampleEvent("E", 5, 12000L)
        );

        // DataStream con tipo BaseEvent
        DataStream<BaseEvent> eventStream = env.fromCollection(events);

        // Strategia di timestamp e watermark
        eventStream = eventStream.assignTimestampsAndWatermarks(
                WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> (Long) event.getFieldValue("timestamp"))

        );

        // Generazione del pattern dalla stringa
        Pattern<BaseEvent, ?> pattern = FlinkCEPPatternGenerator.generatePattern(patternString);

        // Applicazione del pattern al datastream
        PatternStream<BaseEvent> patternStream = CEP.pattern(eventStream, pattern);

        // Estrazione dei risultati
        DataStream<List<Tuple3<String, Integer, Long>>> resultStream = patternStream.select(new PatternSelectFunction<BaseEvent, List<Tuple3<String, Integer, Long>>>() {
            @Override
            public List<Tuple3<String, Integer, Long>> select(Map<String, List<BaseEvent>> pattern) {
                List<Tuple3<String, Integer, Long>> matchedEvents = new ArrayList<>();

                // Iterazione su ogni entry nel pattern per raccogliere tutti gli eventi corrispondenti
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

        // Sink per stampare i risultati
        resultStream.addSink(new SinkFunction<List<Tuple3<String, Integer, Long>>>() {
            @Override
            public void invoke(List<Tuple3<String, Integer, Long>> value, Context context) {
                // Stampa la sequenza completa degli eventi corrispondenti
                System.out.println("Pattern match sequence:");
                for (Tuple3<String, Integer, Long> event : value) {
                    System.out.println(event);
                }
            }
        });

        // Avvio dell'esecuzione di Flink
        env.execute("Flink CEP Pattern Example");
    }
}
