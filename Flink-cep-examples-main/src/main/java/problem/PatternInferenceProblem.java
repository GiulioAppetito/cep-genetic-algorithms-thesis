package problem;

import events.BaseEvent;
import events.source.CsvFileEventSource;
import fitness.FitnessCalculator;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
    private final String filePath;
    private final Set<Pattern<BaseEvent, ?>> truePatterns; // True patterns to match against
    private final Set<List<Map<String, Object>>> targetExtractions;
    private final StreamExecutionEnvironment env;
    private final DataStream<BaseEvent> eventStream;
    private final StringGrammar<String> grammar;

    public PatternInferenceProblem(String filePath, Set<Pattern<BaseEvent, ?>> truePatterns) throws Exception {
        this.filePath = filePath;
        this.truePatterns = truePatterns;

        // Load configuration
        Properties config = loadConfig("config.properties");
        String datasetDirPath = config.getProperty("datasetDirPath");
        String csvFilePath = datasetDirPath + config.getProperty("csvFileName");

        // Initialize Flink environment and load events from CSV
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

        // Read target sequences for fitness evaluation
        String targetDatasetPath = config.getProperty("targetDatasetPath");
        this.targetExtractions = FitnessCalculator.readTargetSequencesFromFile(targetDatasetPath);

        // Load grammar
        this.grammar = loadGrammar(filePath);
    }

    @Override
    public Comparator<Double> totalOrderComparator() {
        return Double::compare;
    }

    @Override
    public Function<PatternRepresentation, Double> qualityFunction() {
        return patternRepresentation -> {
            try {
                // Convert PatternRepresentation to Flink pattern
                Pattern<BaseEvent, ?> generatedPattern = new RepresentationToPatternMapper<BaseEvent>().convert(patternRepresentation);

                // Use existing FitnessCalculator methods
                Set<List<Map<String, Object>>> detectedSequences = FitnessCalculator.collectSequenceMatches(eventStream, List.of(generatedPattern), "Generated");
                return FitnessCalculator.calculateFitnessScore(targetExtractions, detectedSequences);

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
        // Returns a solution mapper that converts a tree to a pattern representation
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
