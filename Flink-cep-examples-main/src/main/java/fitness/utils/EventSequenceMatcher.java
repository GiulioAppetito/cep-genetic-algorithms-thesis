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
    private static final Set<List<Map<String, Object>>> sequencesSet = new CopyOnWriteArraySet<>();

    /**
     * Collects matching sequences from the input data stream using the given pattern.
     */
    public static Set<List<Map<String, Object>>> collectSequenceMatches(StreamExecutionEnvironment createdEnv, DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> generatedPattern, String type, PatternRepresentation.KeyByClause keyByClause) throws Exception {
        System.out.println("[EventSequenceMatcher]: collectSequenceMatches invoked. ");

        // Apply keyBy if a key is specified in the keyByClause
        DataStream<BaseEvent> keyedStream = (keyByClause != null && keyByClause.key() != null) ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key())) : inputDataStream;

        // Create a data stream of matched sequences
        System.out.println("[EventSequenceMatcher]: getting matched datastream. ");
        DataStream<List<Map<String, Object>>> matchedStream = getMatchedDataStream(keyedStream, generatedPattern);

        // Map each matched sequence into the shared set and trigger processing
        System.out.println("[EventSequenceMatcher]: adding sequences to set. ");
        matchedStream.map(new MapFunction<List<Map<String, Object>>, List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> map(List<Map<String, Object>> sequence) {
                sequencesSet.add(sequence); // Add the matched sequence to the shared set
                return sequence;
            }
        }).addSink(new SinkFunction<List<Map<String, Object>>>() {
            @Override
            public void invoke(List<Map<String, Object>> value, Context context) {
                // No-op sink to trigger Flink execution
            }
        });

        // Execute the Flink job
        System.out.println("[EventSequenceMatcher]: executing pattern application. ");
        createdEnv.execute("Event Sequence Matcher");

        // Return the collected set of sequences
        return sequencesSet;
    }

    /**
     * Creates a data stream of matched sequences for a given pattern.
     */
    private static DataStream<List<Map<String, Object>>> getMatchedDataStream(DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> pattern) {
        // Create a pattern stream using the input data stream and the pattern
        System.out.println("[EventSequenceMatcher]: Applying Pattern to inputDataStream. ");
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        System.out.println("[EventSequenceMatcher]: Selecting stream. ");
        // Transform the pattern matches into a list of maps
        return patternStream.select(new PatternToListSelectFunction());
    }

    /**
     * A PatternSelectFunction to convert matches into a list of maps.
     */
    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<Map<String, Object>>> {
        @Override
        public List<Map<String, Object>> select(Map<String, List<BaseEvent>> match) {
            System.out.println("[EventSequenceMatcher]: PatternToListSelectFunction invoked. ");
            // Convert the matched events into a list of maps
            List<Map<String, Object>> resultSequence = new ArrayList<>();

            // Iterate over all values in the map (which are lists of events)
            for (List<BaseEvent> events : match.values()) {
                // Iterate over each event in the list
                for (BaseEvent event : events) {
                    // Add the mapped representation of the event to the result list
                    resultSequence.add(new HashMap<>(event.toMap()));
                }
            }
            System.out.println("[EventSequenceMatcher]: resultSequence = "+ resultSequence);
            return resultSequence;
        }
    }

}
