package app;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Load grammar from the resource file
            InputStream grammarStream = Main.class.getResourceAsStream("/FlinkCEPGrammar.bnf");
            assert grammarStream != null;
            StringGrammar<String> grammar = StringGrammar.load(grammarStream);

            // Parameters for tree generation
            int MAX_HEIGHT = 100;
            int TARGET_DEPTH = 10;

            System.out.println("Loaded Grammar: ");
            System.out.println(grammar);

            // Initialize a factory to generate a tree with a maximum depth
            GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(MAX_HEIGHT, grammar);

            // Generate a random tree
            Tree<String> randomTree = treeFactory.build(new Random(), TARGET_DEPTH);

            if (randomTree != null) {
                System.out.println("Generated Random Tree:");
                randomTree.prettyPrint(System.out);  // Print the structure of the generated tree

                // Apply TreeToRepresentationMapper to convert the tree into PatternRepresentation
                System.out.println("\n********** Applying pattern mapper... **********");
                TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
                PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);

                // Print the resulting PatternRepresentation
                System.out.println("\nMapped PatternRepresentation:\n");
                System.out.println(patternRepresentation);

                // Convert PatternRepresentation into a Flink Pattern
                System.out.println("\n********** Converting to Flink Pattern... **********");
                RepresentationToPatternMapper<Map<String, Object>> toPatternMapper = new RepresentationToPatternMapper<>();
                Pattern<Map<String, Object>, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);

                // Print the generated Flink Pattern
                System.out.println("\nGenerated Flink Pattern:\n");
                System.out.println(flinkPattern);

                // Initialize the Flink environment
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                // Create a DataStream with sample data (Map<String, Object>) and assign timestamps
                DataStream<Map<String, Object>> inputDataStream = env.fromElements(
                        createMutableMap(1L, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f),
                        createMutableMap(2L, 2.5f, 1.0f, 3.5f, 4.5f, 5.5f),
                        createMutableMap(3L, 3.5f, 2.5f, 1.5f, 4.0f, 5.0f),
                        createMutableMap(4L, 1.5f, 2.0f, 3.5f, 4.0f, 5.0f),
                        createMutableMap(5L, 4.0f, 3.0f, 2.0f, 1.0f, 5.0f),
                        createMutableMap(6L, 1.5f, 4.0f, 3.0f, 2.0f, 5.0f),
                        createMutableMap(7L, 2.0f, 3.0f, 4.0f, 1.5f, 5.5f) // Last data point facilitates a match for testing
                ).assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Map<String, Object>>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> (Long) event.get("timestamp"))
                );

                // Apply the pattern to the input data stream
                PatternStream<Map<String, Object>> patternStream = CEP.pattern(inputDataStream, flinkPattern);

                // Select and print the matched events
                DataStream<String> resultStream = patternStream.select(
                        (PatternSelectFunction<Map<String, Object>, String>) matchedEvents -> {
                            StringBuilder result = new StringBuilder("Matched Events: ");
                            matchedEvents.forEach((key, events) -> {
                                for (Map<String, Object> event : events) {
                                    result.append(key).append("=").append(event).append(", ");
                                }
                            });

                            // Remove trailing comma and space
                            if (result.length() > 0) {
                                result.setLength(result.length() - 2);
                            }

                            return result.toString();
                        }
                );

                // Print the match results
                inputDataStream.print("Input DataStream:");
                resultStream.print("Matched Pattern:");

                // Execute the Flink application
                env.execute("CEP Pattern Matching Application with Random Grammar Tree");
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to create mutable maps with all variables from v1 to v5 and a timestamp
    private static Map<String, Object> createMutableMap(Long timestamp, Float v1, Float v2, Float v3, Float v4, Float v5) {
        Map<String, Object> map = new HashMap<>();
        map.put("v1", v1);
        map.put("v2", v2);
        map.put("v3", v3);
        map.put("v4", v4);
        map.put("v5", v5);
        map.put("timestamp", timestamp);
        return map;
    }
}
