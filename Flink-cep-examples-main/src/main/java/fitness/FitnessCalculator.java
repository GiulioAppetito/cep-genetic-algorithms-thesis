package fitness;

import events.BaseEvent;
import fitness.fitnesstypes.FitnessFunctionEnum;
import fitness.utils.EventSequenceMatcher;
import fitness.utils.ScoreCalculator;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import utils.ColoredText;
import org.apache.flink.api.common.JobID;

import java.io.FileWriter;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;

public class FitnessCalculator {

    private final Set<List<Map<String, Object>>> targetSequences;
    private static final Map<Integer, Double> fitnessCache = new ConcurrentHashMap<>();

    private static final String CSV_FILE_PATH = generateDynamicFileName();

    public FitnessCalculator(Set<List<Map<String, Object>>> targetSequences) {
        this.targetSequences = targetSequences;
    }

    private static String generateDynamicFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        return "/RESULTS/" + timestamp + "_job_fitness_and_times.csv";
    }

    public double calculateFitness(
                                   FitnessFunctionEnum fitnessFunction,
                                   StreamExecutionEnvironment env,
                                   DataStream<BaseEvent> inputDataStream,
                                   Pattern<BaseEvent, ?> generatedPattern,
                                   PatternRepresentation.KeyByClause keyByClause,
                                   PatternRepresentation patternRepresentation) throws Exception {

        int patternHash = patternRepresentation.hashCode();
        if (fitnessCache.containsKey(patternHash)) {
            System.out.println(ColoredText.GREEN + ("Fitness cached for hash: " + patternHash) + ColoredText.RESET);
            return fitnessCache.get(patternHash);
        }

        DataStream<BaseEvent> streamToUse = (keyByClause != null && keyByClause.key() != null)
                ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
                : inputDataStream;

        EventSequenceMatcher matcher = new EventSequenceMatcher();
        Map<String, Object> matchResults = matcher.collectSequenceMatches(
                env, streamToUse, generatedPattern, "Generated", keyByClause, patternRepresentation, 60, TimeUnit.SECONDS);

        Set<List<Map<String, Object>>> detectedSequences = (Set<List<Map<String, Object>>>) matchResults.get("detectedSequences");
        String jobId = patternRepresentation.toString();
        double durationSeconds = (double) matchResults.get("durationSeconds");
        int foundMatches = (int) matchResults.get("foundMatches");

        double beta = 1;
        double fitnessScore = ScoreCalculator.calculateFitnessScore(fitnessFunction, targetSequences, detectedSequences, patternRepresentation, beta);
        fitnessCache.put(patternHash, fitnessScore);

        saveFitnessData(jobId, fitnessScore, durationSeconds, foundMatches);

        return fitnessScore;
    }

    private void saveFitnessData(String jobId, double fitness, double duration, int matches) {
        try (FileWriter writer = new FileWriter(CSV_FILE_PATH, true)) {
            writer
                  .append(String.format("%.3f", fitness)).append(",")
                  .append(String.format("%.3f", duration)).append(",")
                  .append(String.valueOf(matches)).append("\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("[ERROR] Could not save fitness data: " + e.getMessage());
        }
    }
}
