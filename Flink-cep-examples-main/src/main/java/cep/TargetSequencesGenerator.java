package cep;

import events.BaseEvent;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class TargetSequencesGenerator {

    // Generate a list of patterns
    public static List<Pattern<BaseEvent, ?>> createTargetPatterns() {
        List<Pattern<BaseEvent, ?>> targetPatterns = new ArrayList<>();
        // Define the pattern for 3 or more failed login attempts for a specific IP address within a time interval
        Pattern<BaseEvent, ?> eventPattern = Pattern
                .<BaseEvent>begin("first_event")
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Map<String, Object> eventMap = event.toMap();
                        Object alarmStatus = eventMap.get("alarm_status");
                        Object temperature = eventMap.get("temperature");

                        // Primo evento con alarm_status=True e temperature > 0
                        return temperature instanceof Number && ((Number) temperature).doubleValue() > 0;
                    }
                })
                .next("second_event")
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Map<String, Object> eventMap = event.toMap();
                        Object vibration = eventMap.get("vibration");

                        // Secondo evento con vibrazione alta e lo stesso sensore
                        boolean isVibrationHigh = vibration instanceof Number && Math.abs(((Number) vibration).doubleValue()) > 500;
                        return isVibrationHigh;
                    }
                });

        targetPatterns.add(eventPattern);
        return targetPatterns;
    }

    // Save matched sequences to a file
    public static void saveMatchesToFile(List<Pattern<BaseEvent, ?>> patterns, DataStream<BaseEvent> inputDataStream,
                                         String targetDatasetPath, String keyByField) throws Exception {
        DataStream<BaseEvent> streamToUse = (keyByField != null && !keyByField.isEmpty())
                ? inputDataStream.keyBy((KeySelector<BaseEvent, Object>) event -> event.toMap().get(keyByField))
                : inputDataStream;

        try (FileWriter writer = new FileWriter(targetDatasetPath)) {
            for (Pattern<BaseEvent, ?> pattern : patterns) {
                PatternStream<BaseEvent> patternStream = CEP.pattern(streamToUse, pattern);
                DataStream<List<BaseEvent>> matchedStream = patternStream.select(new PatternToListSelectFunction());

                Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
                while (iterator.hasNext()) {
                    List<BaseEvent> eventsList = iterator.next();
                    List<Map<String, Object>> sequence = new ArrayList<>();
                    for (BaseEvent event : eventsList) {
                        sequence.add(new HashMap<>(event.toMap()));
                    }
                    writer.write(sequenceToCsvLine(sequence) + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing matched sequences to file", e);
        }
    }

    private static String sequenceToCsvLine(List<Map<String, Object>> sequence) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> map : sequence) {
            builder.append(map.toString().replace(",", ";")).append("|");
        }
        return builder.toString();
    }

    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<BaseEvent>> {
        @Override
        public List<BaseEvent> select(Map<String, List<BaseEvent>> match) {
            List<BaseEvent> collectedEvents = new ArrayList<>();
            match.values().forEach(collectedEvents::addAll);
            return collectedEvents;
        }
    }
}
