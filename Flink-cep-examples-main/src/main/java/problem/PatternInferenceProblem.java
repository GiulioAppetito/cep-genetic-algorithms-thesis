package problem;

import events.BaseEvent;
import events.source.CsvFileEventSource;
import fitness.utils.EventSequenceMatcher;
import fitness.utils.ScoreCalculator;
import fitness.utils.TargetSequenceReader;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import representation.mappers.TreeToRepresentationMapper;
import utils.GrammarGenerator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
    private final Set<List<Map<String, Object>>> targetExtractions;
    private final DataStream<BaseEvent> eventStream;
    private final StringGrammar<String> grammar;

    public PatternInferenceProblem(String configPath) throws Exception {
        // Load configuration properties
        Properties config = loadConfig(configPath);
        String datasetDirPath = config.getProperty("datasetDirPath");
        String csvFilePath = datasetDirPath + config.getProperty("csvFileName");

        // Initialize Flink environment and load events from CSV
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

        // Load target sequences for fitness evaluation
        TargetSequenceReader targetSequenceReader = new TargetSequenceReader();
        String targetDatasetPath = config.getProperty("targetDatasetPath");
        this.targetExtractions = targetSequenceReader.readTargetSequencesFromFile(targetDatasetPath);

        // Generate and load grammar from CSV
        String grammarFilePath = config.getProperty("grammarDirPath") + config.getProperty("grammarFileName");
        GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
        this.grammar = loadGrammar(grammarFilePath);
    }

    @Override
    public Comparator<Double> totalOrderComparator() {
        return (v1, v2) -> Double.compare(v2, v1);
    }

    @Override
    public Function<PatternRepresentation, Double> qualityFunction() {
        return patternRepresentation -> {
            try {
                // Transform PatternRepresentation to Flink CEP Pattern
                Pattern<BaseEvent, ?> generatedPattern = new RepresentationToPatternMapper<BaseEvent>().convert(patternRepresentation);

                // Use EventSequenceMatcher to get detected sequences
                Set<List<Map<String, Object>>> detectedSequences = EventSequenceMatcher.collectSequenceMatches(eventStream, List.of(generatedPattern), "Generated");

                // Compute fitness score
                return ScoreCalculator.calculateFitnessScore(targetExtractions, detectedSequences);
            } catch (Exception e) {
                e.printStackTrace();
                return 0.0;
            }
        };
    }

    @Override
    public StringGrammar<String> getGrammar() {
        return grammar;
    }

    @Override
    public Function<Tree<String>, PatternRepresentation> getSolutionMapper() {
        return new TreeToRepresentationMapper();
    }

    private static Properties loadConfig(String filePath) throws Exception {
        Properties config = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            config.load(input);
        }
        return config;
    }

    private static StringGrammar<String> loadGrammar(String filePath) throws Exception {
        try (InputStream grammarStream = new FileInputStream(filePath)) {
            return StringGrammar.load(grammarStream);
        }
    }
}
