package fitness;

import events.BaseEvent;
import fitness.utils.EventSequenceMatcher;
import fitness.utils.ScoreCalculator;
import fitness.utils.TargetSequenceReader;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FitnessCalculator {

    private final Set<List<Map<String, Object>>> targetSequences;

    public FitnessCalculator(Set<List<Map<String, Object>>> targetSequences) throws Exception {
        // Initialize targetSequences
        this.targetSequences = targetSequences;
    }

    public double calculateFitness(StreamExecutionEnvironment env,
                                   DataStream<BaseEvent> inputDataStream,
                                   Pattern<BaseEvent, ?> generatedPattern,
                                   PatternRepresentation.KeyByClause keyByClause) throws Exception {

        // Apply keyBy if keyByClause is present
        DataStream<BaseEvent> streamToUse = (keyByClause != null && keyByClause.key() != null)
                ? inputDataStream.keyBy(event -> event.toMap().get(keyByClause.key()))
                : inputDataStream;

        // Use EventSequenceMatcher to retrieve the detected sequences
        EventSequenceMatcher matcher = new EventSequenceMatcher();
        Set<List<Map<String, Object>>> detectedSequences = matcher.collectSequenceMatches(env, streamToUse, generatedPattern, "Generated", keyByClause);

        // Use ScoreCalculator to calculate and return the fitness score
        return ScoreCalculator.calculateFitnessScore(targetSequences, detectedSequences, keyByClause);
    }
}
