package problem;

import cep.TargetSequencesGenerator;
import events.BaseEvent;
import events.factory.DataStreamFactory;
import utils.ColoredText;
import utils.CsvAnalyzer;
import fitness.FitnessCalculator;
import fitness.utils.TargetSequenceReader;
import grammar.GrammarGenerator;
import grammar.datatypes.GrammarTypes;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.TreeToRepresentationMapper;

import java.util.*;
import java.util.List;
import java.util.function.Function;

import static utils.Utils.*;

public class  PatternInferenceProblem implements GrammarBasedProblem<String, PatternRepresentation>, TotalOrderQualityBasedProblem<PatternRepresentation, Double> {
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
        StreamExecutionEnvironment localEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<BaseEvent> eventStream = DataStreamFactory.createDataStream(localEnv, csvFilePath);
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
        String type = getRequiredProperty(myConfig, "grammarType");
        GrammarTypes grammarType = GrammarTypes.valueOf(type);

        GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath, grammarType, numEvents);
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
                StreamExecutionEnvironment remoteEnvironment = StreamExecutionEnvironment.createRemoteEnvironment(
                        "localhost", // Hostname del JobManager nel cluster
                        8081,
                        "C:\\Users\\giuli\\IdeaProjects\\cep-genetic-algorithms-thesis-dev3\\Flink-cep-examples-main\\target\\flinkCEP-Patterns-0.1-jar-with-dependencies.jar"
                );
                ExecutionConfig config = remoteEnvironment.getConfig();
                config.registerKryoType(java.util.HashMap.class);
                config.registerKryoType(BaseEvent.class);

                // Create local DataStream from factory
                DataStream<BaseEvent> eventStream = DataStreamFactory.createDataStream(remoteEnvironment, csvFilePath);

                // Calculate fitness of the Pattern
                double fitness =  fitnessCalculator.calculateFitness(
                        remoteEnvironment,
                        eventStream,
                        new representation.mappers.RepresentationToPatternMapper<BaseEvent>().convert(patternRepresentation, duration, numEvents),
                        patternRepresentation.keyByClause(),
                        patternRepresentation);

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

}
