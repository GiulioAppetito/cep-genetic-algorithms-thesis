package cep;

import events.BaseEvent;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import utils.ColoredText;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static utils.Utils.loadConfig;

public class TargetSequencesGenerator {

    // Generate a list of patterns
    public static List<Pattern<BaseEvent, ?>> createTargetPatterns() throws Exception {
        List<Pattern<BaseEvent, ?>> targetPatterns = new ArrayList<>();
        // Define the pattern for 3 or more failed login attempts for a specific IP address within a time interval
        Properties myConfig = loadConfig("src/main/resources/config.properties");
        String targetStrategy = myConfig.getProperty("targetStrategy", "");
        AfterMatchSkipStrategy skipStrategy = switch (targetStrategy) {
            case "noSkip" -> AfterMatchSkipStrategy.noSkip();
            case "skipToNext" -> AfterMatchSkipStrategy.skipToNext();
            case "skipPastLastEvent" -> AfterMatchSkipStrategy.skipPastLastEvent();
            default -> throw new IllegalArgumentException("Invalid AfterMatchSkipStrategy: " + targetStrategy);
        };
        System.out.println(ColoredText.GREEN+"Selected TARGET AfterMatchSkipStrategy: " + skipStrategy+ColoredText.RESET);

        Pattern<BaseEvent, ?> loginPattern = Pattern.<BaseEvent>begin("first_event", skipStrategy)
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Map<String, Object> eventMap = event.toMap();
                        Object alarm_status = eventMap.get("alarm_status");

                        // Primo evento con alarm_status = false
                        return Boolean.FALSE.equals(alarm_status);
                    }
                }).oneOrMore() // Può esserci uno o più eventi con alarm_status = false
                .next("second_event") // Successivamente, un evento con alarm_status = true
                .where(new SimpleCondition<>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Map<String, Object> eventMap = event.toMap();
                        Object alarm_status = eventMap.get("alarm_status");

                        // Evento successivo con alarm_status = true
                        return Boolean.TRUE.equals(alarm_status);
                    }
                });


        targetPatterns.add(loginPattern);
        return targetPatterns;
    }

    // Save matched sequences to a file
    public static void saveMatchesToFile(List<Pattern<BaseEvent, ?>> patterns, DataStream<BaseEvent> inputDataStream, String targetDatasetPath, String keyByField) throws Exception {
        DataStream<BaseEvent> streamToUse = (keyByField != null && !keyByField.isEmpty()) ? inputDataStream.keyBy((KeySelector<BaseEvent, Object>) event -> event.toMap().get(keyByField)) : inputDataStream;

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
                System.out.println(ColoredText.PURPLE+"[TargetSequencesGenerator]: Finished to write the target file."+ColoredText.RESET);
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
