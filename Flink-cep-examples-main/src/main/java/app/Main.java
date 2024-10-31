package app;

import events.BaseEvent;
import events.source.EventSource;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import fitness.FitnessCalculator;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.mappers.TreeToRepresentationMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Load Grammar and Generate Random Tree
            StringGrammar<String> grammar = loadGrammar("/grammars/FlinkCEPGrammar.bnf");
            Tree<String> randomTree = generateRandomTree(grammar);

            if (randomTree != null) {
                // Convert the Tree to PatternRepresentation
                PatternRepresentation patternRepresentation = mapTreeToPattern(randomTree);

                // Convert PatternRepresentation to Flink CEP Pattern
                Pattern<BaseEvent, ?> flinkPattern = mapPatternRepresentationToFlinkPattern(patternRepresentation);

                // Set Up Flink Environment and Create a DataStream
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                int numberOfEvents = 100;
                DataStream<BaseEvent> eventStream = EventSource.generateHardcodedEventDataStream(env, numberOfEvents);

                // Define reference patterns
                List<Pattern<BaseEvent, ?>> referencePatterns = new ArrayList<>();
                Pattern<BaseEvent, BaseEvent> pattern1 = Pattern
                        .<BaseEvent>begin("event1")
                        .where(new SimpleCondition<BaseEvent>() {
                            @Override
                            public boolean filter(BaseEvent event) {
                                return (float) event.toMap().get("v1") > 0.0;
                            }
                        })
                        .next("event2")
                        .where(new SimpleCondition<BaseEvent>() {
                            @Override
                            public boolean filter(BaseEvent event) {
                                return (float) event.toMap().get("v2") < 0.0;
                            }
                        });
                referencePatterns.add(pattern1);
                Pattern<BaseEvent, BaseEvent> pattern2 = Pattern
                        .<BaseEvent>begin("event1")
                        .where(new SimpleCondition<BaseEvent>() {
                            @Override
                            public boolean filter(BaseEvent event) {
                                return (float) event.toMap().get("v3") > 0.0;
                            }
                        })
                        .next("event2")
                        .where(new SimpleCondition<BaseEvent>() {
                            @Override
                            public boolean filter(BaseEvent event) {
                                return (float) event.toMap().get("v4") < 0.0;
                            }
                        });
                referencePatterns.add(pattern2);

                // Calculate fitness
                System.out.println("\n______________________________ Computing fitness... ______________________________\n");
                double fitness = FitnessCalculator.calculateFitness(env, eventStream, referencePatterns, flinkPattern);
                System.out.println("Fitness: " + fitness + "%");
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
        System.out.println("\n______________________________ Applying pattern mapper... ______________________________");
        TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
        PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);
        System.out.println("\nMapped PatternRepresentation:\n");
        System.out.println(patternRepresentation);
        return patternRepresentation;
    }

    // Convert PatternRepresentation to Flink CEP Pattern
    private static Pattern<BaseEvent, ?> mapPatternRepresentationToFlinkPattern(PatternRepresentation patternRepresentation) {
        System.out.println("\n______________________________ Converting to Flink Pattern... ______________________________");
        RepresentationToPatternMapper<BaseEvent> toPatternMapper = new RepresentationToPatternMapper<>();
        Pattern<BaseEvent, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);
        System.out.println("\nGenerated Flink Pattern:\n");
        System.out.println(flinkPattern);
        return flinkPattern;
    }
}
