package fitness;

import events.engineering.BaseEvent;
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

        // Find target sequences from reference patterns
        Set<List<Map<String, Object>>> targetSequences = collectSequenceMatches(inputDataStream, referencePatterns, "Target");

        // Find detected sequences from the generated pattern
        Set<List<Map<String, Object>>> detectedSequences = collectSequenceMatches(inputDataStream, Collections.singletonList(generatedPattern), "Generated");

        // Calculate fitness as the percentage of target sequences detected
        return calculateFitnessScore(targetSequences, detectedSequences);
    }

    private static Set<List<Map<String, Object>>> collectSequenceMatches(DataStream<BaseEvent> inputDataStream, List<Pattern<BaseEvent, ?>> patterns, String type) throws Exception {
        Set<List<Map<String, Object>>> sequencesSet = new HashSet<>();

        for (Pattern<BaseEvent, ?> pattern : patterns) {
            DataStream<List<BaseEvent>> matchedStream = getMatchedDataStream(inputDataStream, pattern);

            Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
            while (iterator.hasNext()) {
                List<BaseEvent> eventsList = iterator.next();

                // Convert the list of events into a list of maps for easier comparison
                List<Map<String, Object>> sequence = new ArrayList<>();
                for (BaseEvent event : eventsList) {
                    sequence.add(new HashMap<>(event.toMap()));
                }

                sequencesSet.add(sequence);
                System.out.println("[" + type + "] match sequence: " + sequence);
            }
        }

        return sequencesSet;
    }

    private static DataStream<List<BaseEvent>> getMatchedDataStream(DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> pattern) {
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        return patternStream.select(new PatternToListSelectFunction());
    }

    private static double calculateFitnessScore(Set<List<Map<String, Object>>> targetSequences, Set<List<Map<String, Object>>> detectedSequences) {
        int targetCount = targetSequences.size();
        int detectedTargetCount = (int) targetSequences.stream().filter(detectedSequences::contains).count();
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
