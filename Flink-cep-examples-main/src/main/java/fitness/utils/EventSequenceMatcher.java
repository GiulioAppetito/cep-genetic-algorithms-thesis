package fitness.utils;

import events.BaseEvent;
import org.apache.flink.api.common.JobID;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.CloseableIterator;
import representation.PatternRepresentation;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.*;

public class EventSequenceMatcher {

    // File path inside the container
    private static final String CSV_FILE_PATH = "/RESULTS/job_times.csv";

    /**
     * Collects matching sequences from the input data stream using the given pattern.
     */
    public Map<String, Object> collectSequenceMatches(
        StreamExecutionEnvironment remoteEnvironment,
        DataStream<BaseEvent> inputDataStream,
        Pattern<BaseEvent, ?> generatedPattern,
        String type,
        PatternRepresentation.KeyByClause keyByClause,
        PatternRepresentation patternRepresentation,
        long timeoutSeconds, 
        TimeUnit timeUnit) throws Exception {

    long startTime = System.nanoTime();

    DataStream<BaseEvent> keyedStream = (keyByClause != null && keyByClause.key() != null)
            ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
            : inputDataStream;

    DataStream<List<Map<String, Object>>> matchedStream = applyPatternToDatastream(keyedStream, generatedPattern);
    Set<List<Map<String, Object>>> detectedSequences = new HashSet<>();
    String jobName = patternRepresentation.toString();

    // Rimuoviamo executeAsync per evitare doppia esecuzione
    int foundMatches = 0;
    boolean completedInTime = false;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Integer> future = executor.submit(() -> {
        int matchCount = 0;
        try (CloseableIterator<List<Map<String, Object>>> iterator = matchedStream.executeAndCollect(jobName)) {
            while (iterator.hasNext()) {
                detectedSequences.add(iterator.next());
                matchCount++;
            }
        }
        return matchCount;
    });

    try {
        foundMatches = future.get(timeoutSeconds, timeUnit);
        completedInTime = true;
    } catch (TimeoutException e) {
        System.err.println("[ERROR] Timeout reached! Cancelling job...");
        future.cancel(true);
        detectedSequences.clear();
    } finally {
        executor.shutdown();
    }

    long endTime = System.nanoTime();
    double durationSeconds = (endTime - startTime) / 1_000_000_000.0;

    System.out.println(completedInTime ? "[LOG] Query completed successfully." : "[WARNING] Query cancelled due to timeout.");
    System.out.println("[LOG] Final detected sequences: " + detectedSequences);

    Map<String, Object> result = new HashMap<>();
    result.put("detectedSequences", detectedSequences);
    result.put("durationSeconds", durationSeconds);
    result.put("foundMatches", foundMatches);

    return result;
}

    /**
     * Creates a data stream of matched sequences for a given pattern.
     */
    private DataStream<List<Map<String, Object>>> applyPatternToDatastream(
            DataStream<BaseEvent> inputDataStream,
            Pattern<BaseEvent, ?> pattern) {

        // Associate the pattern with the data stream
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);

        // Select matches as a list of maps
        return patternStream.select(new PatternToListSelectFunction());
    }

    /**
     * A PatternSelectFunction to convert matches into a list of maps.
     */
    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<Map<String, Object>>> {
        @Override
        public List<Map<String, Object>> select(Map<String, List<BaseEvent>> match) {
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