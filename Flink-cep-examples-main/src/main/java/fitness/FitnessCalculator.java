package fitness;

import events.BaseEvent;
import fitness.utils.EventSequenceMatcher;
import fitness.utils.ScoreCalculator;
import fitness.utils.TargetSequenceReader;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FitnessCalculator {

    private final Set<List<Map<String, Object>>> targetSequences;

    public FitnessCalculator(Properties config) throws Exception {
        // Init targetSequences reading from a configuration file
        String targetDatasetPath = config.getProperty("targetDatasetPath");
        this.targetSequences = TargetSequenceReader.readTargetSequencesFromFile(targetDatasetPath);
    }

    public double calculateFitness(StreamExecutionEnvironment env, DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> generatedPattern) throws Exception {
        // Use EventSequenceMatcher to retrieve the detected sequences
        Set<List<Map<String, Object>>> detectedSequences = EventSequenceMatcher.collectSequenceMatches(inputDataStream, List.of(generatedPattern), "Generated");
        // Use ScoreCalculator to return the fitness
        return ScoreCalculator.calculateFitnessScore(targetSequences, detectedSequences);
    }
}
