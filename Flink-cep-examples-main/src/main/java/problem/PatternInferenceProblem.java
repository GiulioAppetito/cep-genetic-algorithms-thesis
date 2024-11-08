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
import java.util.*;
import java.util.function.Function;

public class PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
    private final Set<Pattern<BaseEvent, ?>> truePatterns;
    private final Set<List<Map<String, Object>>> targetExtractions;
    private final DataStream<BaseEvent> eventStream;
    private final StringGrammar<String> grammar;

    public PatternInferenceProblem(String configPath, Set<Pattern<BaseEvent, ?>> truePatterns) throws Exception {
        this.truePatterns = truePatterns;

        // Load configuration
        Properties config = loadConfig(configPath);
        String datasetDirPath = config.getProperty("datasetDirPath");
        String csvFilePath = datasetDirPath + config.getProperty("csvFileName");

        // Initialize Flink environment and load events from CSV
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

        // Read target sequences for fitness evaluation
        String targetDatasetPath = config.getProperty("targetDatasetPath");
        this.targetExtractions = FitnessCalculator.readTargetSequencesFromFile(targetDatasetPath);

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
        return pr -> {
            try {
                // Transform PatternRepresentation object into a Flink CEP Pattern
                Pattern<BaseEvent, ?> generatedPattern = new RepresentationToPatternMapper<BaseEvent>().convert(pr);

                // Find the extracted events by applying pr on the DataStream field
                Set<List<Map<String, Object>>> detectedSequences = FitnessCalculator.collectSequenceMatches(eventStream, List.of(generatedPattern), "Generated");

                // Compute fitness by comparing the extracted events with the targetExtractions
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
