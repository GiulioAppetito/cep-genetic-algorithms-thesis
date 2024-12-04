package fitness.utils;

import events.BaseEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import representation.PatternRepresentation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventSequenceMatcher {

    // Thread-safe set to collect the matched sequences
    private final Set<List<Map<String, Object>>> sequencesSet = new CopyOnWriteArraySet<>();

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

        // Map each matched sequence into the shared set and trigger processing
        matchedStream.map(new CollectToSetFunction(sequencesSet)) ;// Use a static nested class for MapFunction

        // Execute the Flink job
        createdEnv.execute("Event Sequence Matcher");

        // Return the collected set of sequences
        return sequencesSet;
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

            //System.out.println("[EventSequenceMatcher]: resultSequence = " + resultSequence);
            return resultSequence;
        }
    }

    /**
     * A MapFunction to collect matched sequences into a shared set.
     * This implementation avoids accessing the enclosing class directly.
     */
    private static class CollectToSetFunction
            implements MapFunction<List<Map<String, Object>>, List<Map<String, Object>>> {

        private final Set<List<Map<String, Object>>> targetSet;

        public CollectToSetFunction(Set<List<Map<String, Object>>> targetSet) {
            this.targetSet = targetSet; // Pass the shared set explicitly
        }

        @Override
        public List<Map<String, Object>> map(List<Map<String, Object>> sequence) {
            targetSet.add(sequence); // Add the matched sequence to the set
            return sequence;
        }
    }
}
