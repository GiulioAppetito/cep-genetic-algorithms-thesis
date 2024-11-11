package app;

import events.BaseEvent;
import events.source.CsvFileEventSource;
import fitness.FitnessCalculator;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import representation.mappers.TreeToRepresentationMapper;
import utils.GrammarGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Load configuration properties from config.properties file
            Properties config = loadConfig("config.properties");

            // Define file paths for the dataset and grammar
            String datasetDirPath = config.getProperty("datasetDirPath");
            String csvFileName = config.getProperty("csvFileName");
            String grammarDirPath = config.getProperty("grammarDirPath");
            String grammarFileName = config.getProperty("grammarFileName");
            int maxHeight = Integer.parseInt(config.getProperty("MAX_HEIGHT"));
            int targetDepth = Integer.parseInt(config.getProperty("TARGET_DEPTH"));

            String grammarFilePath = grammarDirPath + grammarFileName;
            String csvFilePath = datasetDirPath + csvFileName;

            // Generate grammar from the CSV file
            System.out.println("Generating grammar from CSV...");
            GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
            System.out.println("Grammar generated at: " + grammarFilePath);

            // Load grammar and generate a random tree structure
            StringGrammar<String> grammar = loadGrammar(grammarFilePath);
            Tree<String> randomTree = generateRandomTree(grammar, maxHeight, targetDepth);

            if (randomTree != null) {
                // Convert the tree structure into a pattern representation
                PatternRepresentation patternRepresentation = mapTreeToPattern(randomTree);

                // Convert the pattern representation into a Flink CEP pattern
                Pattern<BaseEvent, ?> generatedPattern = mapPatternRepresentationToFlinkPattern(patternRepresentation);

                // Set up the Flink environment and load events from the CSV
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                DataStream<BaseEvent> eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

                // Calculate fitness using the FitnessCalculator
                FitnessCalculator fitnessCalculator = new FitnessCalculator(config);
                System.out.println("\n______________________________ Computing fitness... ______________________________\n");
                double fitness = fitnessCalculator.calculateFitness(env, eventStream, generatedPattern);
                System.out.println("Fitness: " + fitness + "%");
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Loads configuration properties from a file
    private static Properties loadConfig(String filePath) throws Exception {
        Properties config = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new FileNotFoundException("Configuration file not found: " + filePath);
            }
            config.load(input);
        }
        return config;
    }

    // Loads grammar from a file
    private static StringGrammar<String> loadGrammar(String filePath) throws Exception {
        System.out.println("\n______________________________ Loading grammar... ______________________________");
        try (InputStream grammarStream = new FileInputStream(filePath)) {
            StringGrammar<String> grammar = StringGrammar.load(grammarStream);
            System.out.println("\nLoaded Grammar:\n ");
            System.out.println(grammar);
            return grammar;
        }
    }

    // Generates a random tree structure based on the provided grammar
    private static Tree<String> generateRandomTree(StringGrammar<String> grammar, int maxHeight, int targetDepth) {
        System.out.println("\n______________________________ Generating random tree from grammar... ______________________________\n");

        GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(maxHeight, grammar);
        Tree<String> randomTree = treeFactory.build(new Random(), targetDepth);

        if (randomTree != null) {
            System.out.println("JGEA Generated Random Tree:\n");
            randomTree.prettyPrint(System.out);
        }

        return randomTree;
    }

    // Maps the generated tree to a pattern representation
    private static PatternRepresentation mapTreeToPattern(Tree<String> randomTree) {
        System.out.println("\n______________________________ Applying pattern mapper... ______________________________\n");
        TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
        PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);
        System.out.println("\nMapped PatternRepresentation:\n");
        System.out.println(patternRepresentation);
        return patternRepresentation;
    }

    // Converts the pattern representation to a Flink CEP pattern
    private static Pattern<BaseEvent, ?> mapPatternRepresentationToFlinkPattern(PatternRepresentation patternRepresentation) {
        System.out.println("\n______________________________ Converting to Flink Pattern... ______________________________");
        RepresentationToPatternMapper<BaseEvent> toPatternMapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);
        System.out.println("\nGenerated Flink Pattern:\n");
        System.out.println(flinkPattern);
        return flinkPattern;
    }
}
