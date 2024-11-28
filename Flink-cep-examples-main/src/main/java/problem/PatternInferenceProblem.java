package problem;

import events.BaseEvent;
import events.source.EventProducer;
import fitness.FitnessCalculator;
import fitness.utils.TargetSequenceReader;

import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import representation.mappers.TreeToRepresentationMapper;
import grammar.GrammarGenerator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
    private final Set<List<Map<String, Object>>> targetExtractions;
    private final DataStream<BaseEvent> eventStream;
    private final StringGrammar<String> grammar;
    private final StreamExecutionEnvironment env;
    private final FitnessCalculator fitnessCalculator;

    public PatternInferenceProblem(String configPath) throws Exception {
        System.out.println("[PatternInferenceProblem]: START.");
        // Load configuration properties with path validation
        Properties myConfig = loadConfig(configPath);
        String datasetDirPath = getRequiredProperty(myConfig, "datasetDirPath");
        String csvFilePath = datasetDirPath + getRequiredProperty(myConfig, "csvFileName");

        // Initialize Flink environment
        System.out.println("[PatternInferenceProblem]: getting env.");
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Register HashMap and other custom classes for Kryo serialization
        System.out.println("[PatternInferenceProblem]: setting kryo.");
        ExecutionConfig config = env.getConfig();
        config.registerKryoType(java.util.HashMap.class);
        config.registerKryoType(events.BaseEvent.class);
        config.registerTypeWithKryoSerializer(events.GenericEvent.class, serializer.GenericEventSerializer.class);

        // Load events from CSV after serializers are registered
        System.out.println("[PatternInferenceProblem]: generating datastream from EventProducer.");
        this.eventStream = EventProducer.generateEventDataStreamFromCSV(env, csvFilePath);

        // Load target sequences (to find) for fitness evaluation
        System.out.println("[PatternInferenceProblem]: retrieving target sequences. ");
        String targetDatasetPath = getRequiredProperty(myConfig, "targetDatasetPath");
        this.targetExtractions = TargetSequenceReader.readTargetSequencesFromFile(targetDatasetPath);

        // Generate and load grammar from CSV
        System.out.println("[PatternInferenceProblem]: generating grammar.");
        String grammarFilePath = getRequiredProperty(myConfig, "grammarDirPath") + getRequiredProperty(myConfig, "grammarFileName");
        GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
        this.grammar = loadGrammar(grammarFilePath);

        System.out.println("[PatternInferenceProblem]: Init fitnessCalculator.");
        // Initialize FitnessCalculator with configuration properties
        this.fitnessCalculator = new FitnessCalculator(myConfig);
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
                PatternRepresentation.KeyByClause keyByClause = patternRepresentation.keyByClause();

                // Calculate fitness using FitnessCalculator
                double fitness = fitnessCalculator.calculateFitness(env, eventStream, generatedPattern, keyByClause);
                return fitness;

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

    // Helper method to retrieve required properties
    private static String getRequiredProperty(Properties config, String propertyName) throws IllegalArgumentException {
        String value = config.getProperty(propertyName);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration property: " + propertyName);
        }
        return value;
    }

    private static StringGrammar<String> loadGrammar(String filePath) throws Exception {
        try (InputStream grammarStream = new FileInputStream(filePath)) {
            return StringGrammar.load(grammarStream);
        }
    }
}
