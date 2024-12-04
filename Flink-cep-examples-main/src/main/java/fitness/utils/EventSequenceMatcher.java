package fitness.utils;

import events.BaseEvent;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.CloseableIterator;
import representation.PatternRepresentation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EventSequenceMatcher {

    /**
     * Collects matching sequences from the input data stream using the given pattern.
     */
    public Set<List<Map<String, Object>>> collectSequenceMatches(
            StreamExecutionEnvironment createdEnv,
            DataStream<BaseEvent> inputDataStream,
            Pattern<BaseEvent, ?> generatedPattern,
            String type,
            PatternRepresentation.KeyByClause keyByClause) throws Exception {

        // Apply keyBy if a key is specified in the keyByClause
        DataStream<BaseEvent> keyedStream = (keyByClause != null && keyByClause.key() != null)
                ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
                : inputDataStream;

        // Create a data stream of matched sequences
        DataStream<List<Map<String, Object>>> matchedStream = getMatchedDataStream(keyedStream, generatedPattern);

        // Collect matched sequences using executeAndCollect()
        Set<List<Map<String, Object>>> detectedSequences = new HashSet<>();

        try (CloseableIterator<List<Map<String, Object>>> iterator = matchedStream.executeAndCollect()) {
            while (iterator.hasNext()) {
                List<Map<String, Object>> sequence = iterator.next();
                detectedSequences.add(sequence);
            }
        }
        return detectedSequences;
    }

    /**
     * Creates a data stream of matched sequences for a given pattern.
     */
    private DataStream<List<Map<String, Object>>> getMatchedDataStream(
            DataStream<BaseEvent> inputDataStream,
            Pattern<BaseEvent, ?> pattern) {

        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        return patternStream.select(new PatternToListSelectFunction());
    }

    /**
     * A PatternSelectFunction to convert matches into a list of maps.
     */
    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<Map<String, Object>>> {
        @Override
        public List<Map<String, Object>> select(Map<String, List<BaseEvent>> match) {
            // Convert the matched events into a list of maps
            List<Map<String, Object>> resultSequence = new ArrayList<>();
            for (List<BaseEvent> events : match.values()) {
                for (BaseEvent event : events) {
                    resultSequence.add(new HashMap<>(event.toMap()));
                }
            }
            return resultSequence;
        }
    }
}
