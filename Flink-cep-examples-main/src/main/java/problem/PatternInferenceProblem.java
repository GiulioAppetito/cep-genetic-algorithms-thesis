package problem;

import events.BaseEvent;
import events.factory.DataStreamFactory;
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
    private final Set<List<Map<String, Object>>> targetSequences;
    private final StringGrammar<String> grammar;
    private final FitnessCalculator fitnessCalculator;
    private final String csvFilePath;

    public PatternInferenceProblem(String configPath) throws Exception {
        // Load configuration properties with path validation
        Properties myConfig = loadConfig(configPath);
        String datasetDirPath = getRequiredProperty(myConfig, "datasetDirPath");
        this.csvFilePath = datasetDirPath + getRequiredProperty(myConfig, "csvFileName");

        // Load target sequences (to find) for fitness evaluation
        String targetDatasetPath = getRequiredProperty(myConfig, "targetDatasetPath");
        this.targetSequences = TargetSequenceReader.readTargetSequencesFromFile(targetDatasetPath);

        // Generate and load grammar from CSV
        String grammarFilePath = getRequiredProperty(myConfig, "grammarDirPath") + getRequiredProperty(myConfig, "grammarFileName");
        GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
        this.grammar = loadGrammar(grammarFilePath);

        // Initialize FitnessCalculator with configuration properties
        this.fitnessCalculator = new FitnessCalculator(targetSequences);
    }

    @Override
    public Comparator<Double> totalOrderComparator() {
        return (v1, v2) -> Double.compare(v2, v1);
    }

    /*
        This function is called for each individual (i.e. each PatternRepresentation)
     */
    @Override
    public Function<PatternRepresentation, Double> qualityFunction() {
        return patternRepresentation -> {
            try {
                // Setup execution environment for Flink
                StreamExecutionEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
                ExecutionConfig config = localEnvironment.getConfig();
                config.registerKryoType(java.util.HashMap.class);
                config.registerKryoType(events.BaseEvent.class);
                config.registerTypeWithKryoSerializer(events.GenericEvent.class, serializer.GenericEventSerializer.class);

                // Generate original DataStream from the CSV file through a factory
                DataStream<BaseEvent> eventStream = DataStreamFactory.createDataStream(localEnvironment, csvFilePath);

                // Convert PatternRepresentation into a Flink CEP Pattern object
                Pattern<BaseEvent, ?> generatedPattern = new RepresentationToPatternMapper<BaseEvent>().convert(patternRepresentation);
                PatternRepresentation.KeyByClause keyByClause = patternRepresentation.keyByClause();

                // Calculate fitness using FitnessCalculator and the generated Pattern in CEP
                double fitness = fitnessCalculator.calculateFitness(localEnvironment, eventStream, generatedPattern, keyByClause);
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
