package fitness;

import events.BaseEvent;
import fitness.utils.EventSequenceMatcher;
import fitness.utils.ScoreCalculator;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import utils.ColoredText;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FitnessCalculator {

    private final Set<List<Map<String, Object>>> targetSequences;

    public FitnessCalculator(Set<List<Map<String, Object>>> targetSequences) {
        // Initialize targetSequences
        this.targetSequences = targetSequences;
    }

    public double calculateFitness(StreamExecutionEnvironment env,
                                   DataStream<BaseEvent> inputDataStream,
                                   Pattern<BaseEvent, ?> generatedPattern,
                                   PatternRepresentation.KeyByClause keyByClause,
                                   PatternRepresentation patternRepresentation) throws Exception {

        // Start measuring time
        long startTime = System.nanoTime();

        // Apply keyBy if keyByClause is present
        DataStream<BaseEvent> streamToUse = (keyByClause != null && keyByClause.key() != null)
                ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
                : inputDataStream;

        // Use EventSequenceMatcher to retrieve the detected sequences
        EventSequenceMatcher matcher = new EventSequenceMatcher();
        Set<List<Map<String, Object>>> detectedSequences = matcher.collectSequenceMatches(env, streamToUse, generatedPattern, "Generated", keyByClause,"output.csv");

        /*
         Use ScoreCalculator to calculate and return the fitness score
         */
        double beta = 1; // Beta value for Fβ score; beta is chosen such that recall is considered beta times as important as precision
        double fitnessScore =  ScoreCalculator.calculateFitnessScore(targetSequences, detectedSequences, patternRepresentation, beta);

        // Elapsed time
        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println(ColoredText.CYAN+"Time taken to evaluate:\nPattern:\n "+ patternRepresentation+":\nTime:"+elapsedTime+"s"+ColoredText.RESET);
        return fitnessScore;
    }
}
