package app;

import events.BaseEvent;
import events.custom.LoginEvent;
import events.source.FileEventSource;
import fitness.FitnessCalculator;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.windowing.time.Time;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import representation.mappers.TreeToRepresentationMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Step 1: Load the grammar and generate a random tree based on the grammar
            StringGrammar<String> grammar = loadGrammar("/grammars/LoginCEPGrammar.bnf");
            Tree<String> randomTree = generateRandomTree(grammar);

            // Step 2: If a random tree was successfully generated, proceed
            if (randomTree != null) {
                // Step 3: Convert the random tree into a PatternRepresentation
                PatternRepresentation patternRepresentation = mapTreeToPattern(randomTree);

                // Step 4: Convert the PatternRepresentation into a Flink CEP pattern
                Pattern<BaseEvent, ?> generatedFlinkPattern = mapPatternRepresentationToFlinkPattern(patternRepresentation);

                // Step 5: Set up the Flink streaming environment
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                // Step 6: Load event data from a CSV file into a data stream
                String csvFilePath = "Flink-cep-examples-main/src/main/resources/datasets/athena-sshd-processed-simple.csv";
                DataStream<BaseEvent> eventStream = FileEventSource.generateLoginEventDataStreamFromCSV(env, csvFilePath);

                // Step 7: Key the data stream by IP address, which will help group events by IP
                KeyedStream<BaseEvent, String> keyedStream = eventStream.keyBy(event -> ((LoginEvent) event).getIpAddress());

                // Step 8: Define a static pattern for fitness calculation (target pattern)
                Pattern<BaseEvent, BaseEvent> targetPattern1 = Pattern
                        .<BaseEvent>begin("firstEvent")
                        .where(new SimpleCondition<BaseEvent>() {
                            @Override
                            public boolean filter(BaseEvent event) {
                                // Only match LoginEvent events
                                return event instanceof LoginEvent;
                            }
                        })
                        .within(Time.seconds(30)); // Define the window of time in which the events should occur

                // Step 9: Create a list of reference patterns and add the target pattern to it
                List<Pattern<BaseEvent, ?>> referencePatterns = new ArrayList<>();
                referencePatterns.add(targetPattern1);

                // Step 10: Set the maximum number of matches to print for each pattern
                int maxMatchesToPrint = 1;

                // Step 11: Calculate the fitness score between the reference pattern and the generated pattern
                double fitness = FitnessCalculator.calculateFitness(env, keyedStream, referencePatterns, generatedFlinkPattern, maxMatchesToPrint);
                System.out.println("Fitness: " + fitness + "%");

                // Step 12: Add a dummy sink to complete the Flink job
                keyedStream.addSink(new DiscardingSink<>());

                // Step 13: Execute the Flink job
                env.execute("Flink CEP Pattern Matching with Static and Generated Patterns");

            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load the grammar from a specified resource file
    private static StringGrammar<String> loadGrammar(String resourcePath) throws Exception {
        System.out.println("\n______________________________ Loading grammar... ______________________________");
        InputStream grammarStream = Main.class.getResourceAsStream(resourcePath);
        assert grammarStream != null; // Ensure the grammar file exists
        StringGrammar<String> grammar = StringGrammar.load(grammarStream);
        System.out.println("\nLoaded Grammar: \n");
        System.out.println(grammar);
        return grammar;
    }

    // Method to generate a random tree using the loaded grammar
    private static Tree<String> generateRandomTree(StringGrammar<String> grammar) {
        System.out.println("\n______________________________ Generating random tree... ______________________________");
        int MAX_HEIGHT = 100; // Define maximum tree height
        int TARGET_DEPTH = 8; // Define the desired depth for the generated tree

        // Use a factory to generate a tree based on the grammar and desired depth
        GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(MAX_HEIGHT, grammar);
        Tree<String> randomTree = treeFactory.build(new Random(), TARGET_DEPTH);

        if (randomTree != null) {
            System.out.println("\nJGEA Generated Random Tree:\n");
            randomTree.prettyPrint(System.out); // Print the generated tree
        }

        return randomTree;
    }

    // Method to map a generated tree to a PatternRepresentation
    private static PatternRepresentation mapTreeToPattern(Tree<String> randomTree) {
        System.out.println("\n______________________________ Applying pattern mapper... ______________________________");
        TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
        PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);
        System.out.println("\nMapped PatternRepresentation:\n");
        System.out.println(patternRepresentation); // Print the mapped PatternRepresentation
        return patternRepresentation;
    }

    // Method to convert a PatternRepresentation into a Flink CEP pattern
    private static Pattern<BaseEvent, ?> mapPatternRepresentationToFlinkPattern(PatternRepresentation patternRepresentation) {
        System.out.println("\n______________________________ Converting to Flink Pattern... ______________________________");
        RepresentationToPatternMapper<BaseEvent> toPatternMapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);
        System.out.println("\nGenerated Flink Pattern:\n");
        return flinkPattern;
    }
}
