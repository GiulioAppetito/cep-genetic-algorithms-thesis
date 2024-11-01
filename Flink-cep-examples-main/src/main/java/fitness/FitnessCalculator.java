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

    // Calculates the fitness score of a generated pattern by comparing it with reference patterns.
    public static double calculateFitness(
            StreamExecutionEnvironment env,
            DataStream<BaseEvent> inputDataStream,
            List<Pattern<BaseEvent, ?>> referencePatterns,
            Pattern<BaseEvent, ?> generatedPattern,
            int maxMatchesToPrint) throws Exception {

        // Collect and print unique sequences from reference patterns
        Set<List<Map<String, Object>>> targetSequences = collectAndPrintMatches(inputDataStream, referencePatterns, maxMatchesToPrint, "Target Pattern");

        // Collect and print unique sequences from the generated pattern
        Set<List<Map<String, Object>>> detectedSequences = collectAndPrintMatches(inputDataStream, Collections.singletonList(generatedPattern), maxMatchesToPrint, "Generated Pattern");

        // Calculate fitness as the percentage of target sequences detected
        return calculateFitnessScore(targetSequences, detectedSequences);
    }

    // Collects and prints the matches of patterns on the input data stream.
    private static Set<List<Map<String, Object>>> collectAndPrintMatches(DataStream<BaseEvent> inputDataStream, List<Pattern<BaseEvent, ?>> patterns, int maxMatchesToPrint, String patternType) throws Exception {
        Set<List<Map<String, Object>>> eventsSet = new HashSet<>();
        int printedMatches = 0;

        for (Pattern<BaseEvent, ?> pattern : patterns) {
            // Get the matched data stream for the given pattern
            DataStream<List<BaseEvent>> matchedStream = getMatchedDataStream(inputDataStream, pattern);

            // Iterate over matched sequences and print unique ones
            Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
            while (iterator.hasNext() && printedMatches < maxMatchesToPrint) {
                List<BaseEvent> eventsList = iterator.next();
                List<Map<String, Object>> sequenceRepresentation = new ArrayList<>();

                // Convert each event to a map representation for printing
                for (BaseEvent event : eventsList) {
                    sequenceRepresentation.add(event.toMap());
                }

                // Only print if the sequence is unique
                if (eventsSet.add(sequenceRepresentation)) {
                    System.out.println(patternType + " Match Sequence: " + sequenceRepresentation);
                    printedMatches++;
                }
            }
        }
        return eventsSet;
    }

    // Retrieves the matched data stream for the specified pattern.
    private static DataStream<List<BaseEvent>> getMatchedDataStream(DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> pattern) {
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        return patternStream.select(new PatternToListSelectFunction());
    }

    // Calculates the fitness score as the percentage of target sequences detected.
    private static double calculateFitnessScore(Set<List<Map<String, Object>>> targetSequences, Set<List<Map<String, Object>>> detectedSequences) {
        int targetCount = targetSequences.size();
        int detectedTargetCount = (int) targetSequences.stream().filter(detectedSequences::contains).count();
        return targetCount == 0 ? 0.0 : (double) detectedTargetCount / targetCount * 100.0;
    }

    // Inner class to transform pattern matches to a list of events.
    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<BaseEvent>> {
        @Override
        public List<BaseEvent> select(Map<String, List<BaseEvent>> match) {
            List<BaseEvent> collectedEvents = new ArrayList<>();
            match.values().forEach(collectedEvents::addAll);
            return collectedEvents;
        }
    }
}
