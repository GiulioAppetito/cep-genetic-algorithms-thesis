package app;

import events.BaseEvent;
import events.custom.ExampleEvent;
import events.source.EventSource;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.mappers.TreeToRepresentationMapper;

import java.io.InputStream;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Load Grammar and Generate Random Tree
            StringGrammar<String> grammar = loadGrammar("/FlinkCEPGrammar.bnf");
            Tree<String> randomTree = generateRandomTree(grammar);

            if (randomTree != null) {
                // Convert the Tree to PatternRepresentation
                PatternRepresentation patternRepresentation = mapTreeToPattern(randomTree);

                // Convert PatternRepresentation to Flink Pattern
                Pattern<BaseEvent, ?> flinkPattern = mapPatternRepresentationToFlinkPattern(patternRepresentation);

                // Set Up Flink Environment and Create DataStream
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                // Generate a specified number of ExampleEvents with random data
                int numberOfEvents = 100;
                DataStream<BaseEvent> eventStream = EventSource.generateHardcodedEventDataStream(env, numberOfEvents);

                // Apply the Pattern and Print Results
                applyPatternAndPrintResults(env, eventStream, flinkPattern);

                // Execute the Flink job
                env.execute("CEP Pattern Matching Application with Random Grammar Tree");
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load Grammar from Resource File
    private static StringGrammar<String> loadGrammar(String resourcePath) throws Exception {
        InputStream grammarStream = Main.class.getResourceAsStream(resourcePath);
        assert grammarStream != null;
        StringGrammar<String> grammar = StringGrammar.load(grammarStream);
        System.out.println("Loaded Grammar: ");
        System.out.println(grammar);
        return grammar;
    }

    // Generate Random Tree Using Grammar
    private static Tree<String> generateRandomTree(StringGrammar<String> grammar) {
        int MAX_HEIGHT = 100;
        int TARGET_DEPTH = 7;

        GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(MAX_HEIGHT, grammar);
        Tree<String> randomTree = treeFactory.build(new Random(), TARGET_DEPTH);

        if (randomTree != null) {
            System.out.println("Generated Random Tree:");
            randomTree.prettyPrint(System.out);
        }

        return randomTree;
    }

    // Convert Tree to PatternRepresentation
    private static PatternRepresentation mapTreeToPattern(Tree<String> randomTree) {
        System.out.println("\n********** Applying pattern mapper... **********");
        TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
        PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);
        System.out.println("\nMapped PatternRepresentation:\n");
        System.out.println(patternRepresentation);
        return patternRepresentation;
    }

    // Convert PatternRepresentation to Flink Pattern
    private static Pattern<BaseEvent, ?> mapPatternRepresentationToFlinkPattern(PatternRepresentation patternRepresentation) {
        System.out.println("\n********** Converting to Flink Pattern... **********");
        RepresentationToPatternMapper<BaseEvent> toPatternMapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);
        System.out.println("\nGenerated Flink Pattern:\n");
        System.out.println(flinkPattern);
        return flinkPattern;
    }

    // Apply Pattern and Print Results
    private static void applyPatternAndPrintResults(StreamExecutionEnvironment env, DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> flinkPattern) {
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, flinkPattern);

        DataStream<String> resultStream = patternStream.select(
                (PatternSelectFunction<BaseEvent, String>) matchedEvents -> {
                    StringBuilder result = new StringBuilder("Matched Events: ");
                    matchedEvents.forEach((key, events) -> {
                        for (BaseEvent event : events) {
                            result.append(key).append("=").append(event.toMap()).append(", ");
                        }
                    });

                    // Remove comma and space
                    if (result.length() > 0) {
                        result.setLength(result.length() - 2);
                    }

                    return result.toString();
                }
        );

        // Print all matches
        resultStream.print();
    }
}
