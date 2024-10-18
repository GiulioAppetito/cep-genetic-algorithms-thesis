package cep.app;

import cep.events.ExampleEvent;
import cep.events.BaseEvent;
import cep.utils.PatternFitnessEvaluator;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create a Flink environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define the full DataStream of events (the entire sequence of events)
        DataStream<BaseEvent> eventStream = env.fromCollection(Arrays.asList(
                new ExampleEvent("A", 10, 1000L),
                new ExampleEvent("B", 14, 2000L),
                new ExampleEvent("C", 7, 3000L),
                new ExampleEvent("D", 5, 4000L),
                new ExampleEvent("E", 10, 5000L),
                new ExampleEvent("F", 14, 6000L),
                new ExampleEvent("G", 7, 7000L),
                new ExampleEvent("H", 10, 8000L),
                new ExampleEvent("I", 14, 9000L),
                new ExampleEvent("J", 7, 10000L)
        ));

        // Define the static list of target event sequences (subsequences from the full event stream)
        List<List<BaseEvent>> targetSequences = Arrays.asList(
                Arrays.asList(
                        new ExampleEvent("A", 10, 1000L),
                        new ExampleEvent("B", 14, 2000L),
                        new ExampleEvent("C", 7, 3000L)
                ),
                Arrays.asList(
                        new ExampleEvent("H", 10, 8000L),
                        new ExampleEvent("I", 14, 9000L),
                        new ExampleEvent("J", 7, 10000L)
                )
        );

        // Use PatternFitnessEvaluator to evaluate the fitness of a pattern on the full event stream and the target sequences
        PatternFitnessEvaluator evaluator = new PatternFitnessEvaluator();

        // Define the pattern to evaluate its fitness (as a string pattern for simplicity)
        String testPatternString = "begin event1 where value == 10 next event2 where value == 14 followedBy event3 where value == 7";

        // Evaluate the fitness of the pattern using the full event stream and the static target sequences
        double accuracy = evaluator.evaluateFitnessOnFullStream(testPatternString, eventStream, targetSequences);

        // Print the accuracy
        System.out.println("Pattern accuracy: " + accuracy);
    }
}
