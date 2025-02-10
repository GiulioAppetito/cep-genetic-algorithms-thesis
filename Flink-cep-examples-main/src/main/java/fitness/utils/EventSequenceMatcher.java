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
    public Set<List<Map<String, Object>>> collectSequenceMatches(
            StreamExecutionEnvironment remoteEnvironment,
            DataStream<BaseEvent> inputDataStream,
            Pattern<BaseEvent, ?> generatedPattern,
            String type,
            PatternRepresentation.KeyByClause keyByClause,
            String outputCsvPath,
            PatternRepresentation patternRepresentation,
            long timeoutSeconds, // Timeout in seconds
            TimeUnit timeUnit) throws Exception {

        long startTime = System.nanoTime(); // Start time for job execution

        // Apply keyBy clause if specified
        DataStream<BaseEvent> keyedStream = (keyByClause != null && keyByClause.key() != null)
                ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
                : inputDataStream;

        // Apply the pattern to the data stream
        DataStream<List<Map<String, Object>>> matchedStream = applyPatternToDatastream(keyedStream, generatedPattern);
        Set<List<Map<String, Object>>> detectedSequences = new HashSet<>();
        String jobName = patternRepresentation.toString();

        // Start Flink job asynchronously
        JobClient jobClient = remoteEnvironment.executeAsync(jobName);
        JobID jobId = jobClient.getJobID();

        // Use an ExecutorService to collect results asynchronously
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

        int foundMatches = 0;
        boolean completedInTime = false;

        try {
            // Wait for job completion within the timeout
            foundMatches = future.get(timeoutSeconds, timeUnit);
            jobClient.getJobExecutionResult().get(timeoutSeconds, timeUnit);  // Ensure Flink job completes
            completedInTime = true;
        } catch (TimeoutException e) {
            System.err.println("[ERROR] Timeout reached! Cancelling job...");
            future.cancel(true);  // Stop result collection
            jobClient.cancel();   // Cancel Flink job
            detectedSequences.clear();  // Clear results to return an empty set
        } finally {
            executor.shutdown();  // Shutdown the executor service
        }

        long endTime = System.nanoTime(); // End time
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds


        // Log the final query status
        if (completedInTime) {
            System.out.println("[LOG] Query completed successfully.");
            System.out.println("[LOG] Total duration: " + String.format("%.3f", durationSeconds) + " seconds");
            System.out.println("[LOG] Number of matches found: " + foundMatches);
        } else {
            System.out.println("[WARNING] Query cancelled due to timeout.");
        }

        System.out.println("[LOG] Final detected sequences: " + detectedSequences);
        // Save execution time to file
        saveExecutionTime(jobId, durationSeconds,foundMatches);
        return detectedSequences;  // Return results only if job completed within the timeout
    }

    /**
     * Saves execution time to a CSV file inside the container.
     */
    private void saveExecutionTime(JobID jobId, double durationSeconds, int foundMatches) {
        try (FileWriter writer = new FileWriter(CSV_FILE_PATH, true)) { // 'true' enables append mode
            writer.append(jobId.toString())
                .append(",")
                .append(String.format("%.3f", durationSeconds))
                .append(",")
                .append(String.format("%d", foundMatches))
                .append("\n");
            writer.flush();
            System.out.println("[LOG] Execution time saved: " + jobId + " -> " + String.format("%.3f", durationSeconds) + " seconds");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save execution time: " + e.getMessage());
        }
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
