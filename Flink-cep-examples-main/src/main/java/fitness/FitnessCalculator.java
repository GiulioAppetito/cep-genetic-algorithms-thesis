
package fitness;

import events.BaseEvent;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.*;

public class FitnessCalculator {

    public static double calculateFitness(
            StreamExecutionEnvironment env,
            DataStream<BaseEvent> inputDataStream,
            List<Pattern<BaseEvent, ?>> referencePatterns,
            Pattern<BaseEvent, ?> generatedPattern) throws Exception {

        // Find target events from reference patterns
        Set<Map<String, Object>> targetEvents = collectMatches(inputDataStream, referencePatterns, "Target");

        // Find detected events from the generated pattern
        Set<Map<String, Object>> detectedEvents = collectMatches(inputDataStream, Collections.singletonList(generatedPattern), "Generated");

        // Calculate fitness as the percentage of target events detected
        return calculateFitnessScore(targetEvents, detectedEvents);
    }

    private static Set<Map<String, Object>> collectMatches(DataStream<BaseEvent> inputDataStream, List<Pattern<BaseEvent, ?>> patterns, String type) throws Exception {
        Set<Map<String, Object>> eventsSet = new HashSet<>();

        for (Pattern<BaseEvent, ?> pattern : patterns) {
            DataStream<List<BaseEvent>> matchedStream = getMatchedDataStream(inputDataStream, pattern);

            Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
            while (iterator.hasNext()) {
                List<BaseEvent> eventsList = iterator.next();
                for (BaseEvent event : eventsList) {
                    eventsSet.add(new HashMap<>(event.toMap()));
                    System.out.println("[" + type + "] " + "match: " + event.toMap());
                }
            }
        }

        return eventsSet;
    }

    private static DataStream<List<BaseEvent>> getMatchedDataStream(DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> pattern) {
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        return patternStream.select(new PatternToListSelectFunction());
    }

    private static double calculateFitnessScore(Set<Map<String, Object>> targetEvents, Set<Map<String, Object>> detectedEvents) {
        int targetCount = targetEvents.size();
        int detectedTargetCount = (int) targetEvents.stream().filter(detectedEvents::contains).count();
        return targetCount == 0 ? 0.0 : (double) detectedTargetCount / targetCount * 100.0;
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
