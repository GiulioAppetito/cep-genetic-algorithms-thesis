package problem;

import cep.TargetSequencesGenerator;
import events.BaseEvent;
import events.factory.DataStreamFactory;
import events.utils.CsvAnalyzer;
import fitness.FitnessCalculator;
import fitness.utils.TargetSequenceReader;
import grammar.GrammarGenerator;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.TreeToRepresentationMapper;

import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;

public class PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
    private final Set<List<Map<String, Object>>> targetSequences;
    private final StringGrammar<String> grammar;
    private final FitnessCalculator fitnessCalculator;
    private final String csvFilePath;
    private final String targetDatasetPath;
    private final long duration;
    private final long numEvents;

    public PatternInferenceProblem(String configPath) throws Exception {
        Properties myConfig = loadConfig(configPath);

        String datasetDirPath = getRequiredProperty(myConfig, "datasetDirPath");
        this.csvFilePath = datasetDirPath + getRequiredProperty(myConfig, "csvFileName");
        this.targetDatasetPath = getRequiredProperty(myConfig, "targetDatasetPath");
        this.duration = CsvAnalyzer.calculateDurationFromCsv(csvFilePath);
        this.numEvents = CsvAnalyzer.countRowsInCsv(csvFilePath);

        // Generate target patterns and save matches to a file
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        DataStream<BaseEvent> eventStream = DataStreamFactory.createDataStream(env, csvFilePath);
        TargetSequencesGenerator.saveMatchesToFile(
                TargetSequencesGenerator.createTargetPatterns(),
                eventStream,
                targetDatasetPath,
                myConfig.getProperty("targetKeyByField")
        );

        // Load target sequences (to find) for fitness evaluation
        this.targetSequences = TargetSequenceReader.readTargetSequencesFromFile(targetDatasetPath);

        // Generate, load grammar and initialize FitnessCalculator
        String grammarFilePath = getRequiredProperty(myConfig, "grammarDirPath") + getRequiredProperty(myConfig, "grammarFileName");
        GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
        this.grammar = loadGrammar(grammarFilePath);
        this.fitnessCalculator = new FitnessCalculator(targetSequences);
    }

    @Override
    public Comparator<Double> totalOrderComparator() {
        return Comparator.reverseOrder();
    }

    @Override
    public Function<PatternRepresentation, Double> qualityFunction() {
        return patternRepresentation -> {
            try {
                // Setup local environment for Flink CEP
                StreamExecutionEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
                localEnvironment.setParallelism(8); // Set n to the number of available cores
                ExecutionConfig config = localEnvironment.getConfig();
                config.registerKryoType(java.util.HashMap.class);
                config.registerKryoType(BaseEvent.class);


                // Create local DataStream from factory
                DataStream<BaseEvent> eventStream = DataStreamFactory.createDataStream(localEnvironment, csvFilePath);

                // Calculate fitness of the Pattern
                double fitness =  fitnessCalculator.calculateFitness(localEnvironment, eventStream,
                        new representation.mappers.RepresentationToPatternMapper<BaseEvent>().convert(patternRepresentation, duration, numEvents),
                        patternRepresentation.keyByClause());
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
        try (FileInputStream input = new FileInputStream(filePath)) {
            config.load(input);
        }
        return config;
    }

    private static String getRequiredProperty(Properties config, String propertyName) {
        String value = config.getProperty(propertyName);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration property: " + propertyName);
        }
        return value;
    }

    private static StringGrammar<String> loadGrammar(String filePath) throws Exception {
        try (FileInputStream grammarStream = new FileInputStream(filePath)) {
            return StringGrammar.load(grammarStream);
        }
    }
}
