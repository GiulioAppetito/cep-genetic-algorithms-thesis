package cep.utils;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatternFitnessEvaluatorTest {

    @Test
    void testEvaluateFitnessWithMatchingSequence() throws Exception {
        // Setup environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define the event stream
        DataStream<BaseEvent> eventStream = env.fromCollection(Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 14, 2000L),
                new ExampleEvent("C", 7, 3000L)
        ));

        // Define the target sequence
        List<List<BaseEvent>> targetSequences = Arrays.asList(
                Arrays.asList(
                        new ExampleEvent("A", 10, 1000L),
                        new ExampleEvent("B", 14, 2000L),
                        new ExampleEvent("C", 7, 3000L)
                )
        );

        // Create evaluator instance
        PatternFitnessEvaluator evaluator = new PatternFitnessEvaluator();

        // Define the pattern to test
        String testPatternString = "begin event1 where value == 10 next event2 where value == 14 followedBy event3 where value == 7";

        // Evaluate fitness
        double accuracy = evaluator.evaluateFitness(testPatternString, eventStream, targetSequences);

        // Assert the accuracy is 100%
        assertEquals(1.0, accuracy, "The pattern should match exactly with 100% accuracy.");
    }

    @Test
    void testEvaluateFitnessWithNonMatchingSequence() throws Exception {
        // Setup environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define the event stream
        DataStream<BaseEvent> eventStream = env.fromCollection(Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 14, 2000L),
                new ExampleEvent("D", 8, 3000L)
        ));

        // Define the target sequence that should not match
        List<List<BaseEvent>> targetSequences = Arrays.asList(
                Arrays.asList(
                        new ExampleEvent("A", 10, 1000L),
                        new ExampleEvent("B", 14, 2000L),
                        new ExampleEvent("C", 7, 3000L)
                )
        );

        // Create evaluator instance
        PatternFitnessEvaluator evaluator = new PatternFitnessEvaluator();

        // Define the pattern to test
        String testPatternString = "begin event1 where value == 10 next event2 where value == 14 followedBy event3 where value == 7";

        // Evaluate fitness
        double accuracy = evaluator.evaluateFitness(testPatternString, eventStream, targetSequences);

        // Assert that accuracy is 0 because no sequence matches
        assertEquals(0.0, accuracy, "The pattern should not match any sequence, accuracy should be 0.");
    }

    @Test
    void testEvaluateFitnessWithMatchingSequenceFollowedBy() throws Exception {
        // Setup environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define the event stream
        DataStream<BaseEvent> eventStream = env.fromCollection(Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 14, 2000L),
                new ExampleEvent("C", 7, 3000L),
                new ExampleEvent("D", 10, 4000L),
                new ExampleEvent("E", 14, 5000L),
                new ExampleEvent("F", 7, 6000L)
        ));

        // Define the target sequence
        List<List<BaseEvent>> targetSequences = Arrays.asList(
                Arrays.asList(
                        new ExampleEvent("A", 10, 1000L),
                        new ExampleEvent("C", 7, 3000L)
                ),
                Arrays.asList(
                        new ExampleEvent("D", 10, 4000L),
                        new ExampleEvent("F", 7, 6000L)
                )
        );

        // Create evaluator instance
        PatternFitnessEvaluator evaluator = new PatternFitnessEvaluator();

        // Define the pattern to test
        String testPatternString = "begin event1 where value == 10 followedBy event2 where value == 7";

        // Evaluate fitness
        double accuracy = evaluator.evaluateFitness(testPatternString, eventStream, targetSequences);

        // Assert the accuracy is 100%
        assertEquals(1.0, accuracy, "The pattern should match exactly with 100% accuracy.");
    }

}
