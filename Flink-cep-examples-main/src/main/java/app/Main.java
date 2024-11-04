package app;

import cep.CEPTargetPatternFactory;
import events.engineering.BaseEvent;
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
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Paths for CSV and grammar files
            String datasetDirPath = "Flink-cep-examples-main/src/main/resources/datasets/";
            String csvFileName = "ithaca-sshd-processed-simple.csv";

            String grammarDirPath = "Flink-cep-examples-main/src/main/resources/grammars/generated/";
            String grammarFileName = "generatedGrammar.bnf";

            String grammarFilePath = grammarDirPath + grammarFileName;
            String csvFilePath = datasetDirPath + csvFileName;

            // Generate grammar from CSV
            System.out.println("Generating grammar from CSV...");
            GrammarGenerator.generateGrammar(csvFilePath, grammarFilePath);
            System.out.println("Grammar generated at: " + grammarFilePath);

            // Load grammar and generate a random tree
            StringGrammar<String> grammar = loadGrammar(grammarFilePath);
            Tree<String> randomTree = generateRandomTree(grammar);

            if (randomTree != null) {
                // Convert tree to pattern representation
                PatternRepresentation patternRepresentation = mapTreeToPattern(randomTree);

                // Convert pattern representation to Flink CEP pattern
                Pattern<BaseEvent, ?> generatedPattern = mapPatternRepresentationToFlinkPattern(patternRepresentation);

                // Set up Flink environment and load events from CSV
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                DataStream<BaseEvent> eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

                // Define reference patterns for fitness calculation using PatternFactory
                List<Pattern<BaseEvent, ?>> targetPatterns = CEPTargetPatternFactory.createReferencePatterns();

                // Calculate fitness
                System.out.println("\n______________________________ Computing fitness... ______________________________\n");
                double fitness = FitnessCalculator.calculateFitness(env, eventStream, targetPatterns, generatedPattern);
                System.out.println("Fitness: " + fitness + "%");
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StringGrammar<String> loadGrammar(String filePath) throws Exception {
        try (InputStream grammarStream = new FileInputStream(filePath)) {
            StringGrammar<String> grammar = StringGrammar.load(grammarStream);
            System.out.println("Loaded Grammar: ");
            System.out.println(grammar);
            return grammar;
        }
    }

    private static Tree<String> generateRandomTree(StringGrammar<String> grammar) {
        int MAX_HEIGHT = 100;
        int TARGET_DEPTH = 8;

        GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(MAX_HEIGHT, grammar);
        Tree<String> randomTree = treeFactory.build(new Random(), TARGET_DEPTH);

        if (randomTree != null) {
            System.out.println("JGEA Generated Random Tree:");
            randomTree.prettyPrint(System.out);
        }

        return randomTree;
    }

    private static PatternRepresentation mapTreeToPattern(Tree<String> randomTree) {
        System.out.println("\n______________________________ Applying pattern mapper... ______________________________");
        TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
        PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);
        System.out.println("\nMapped PatternRepresentation:\n");
        System.out.println(patternRepresentation);
        return patternRepresentation;
    }

    private static Pattern<BaseEvent, ?> mapPatternRepresentationToFlinkPattern(PatternRepresentation patternRepresentation) {
        System.out.println("\n______________________________ Converting to Flink Pattern... ______________________________");
        RepresentationToPatternMapper<BaseEvent> toPatternMapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);
        System.out.println("\nGenerated Flink Pattern:\n");
        System.out.println(flinkPattern);
        return flinkPattern;
    }
}
