package cep.utils;

import cep.events.BaseEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatternFitnessEvaluator {

    private final StreamExecutionEnvironment env;

    public PatternFitnessEvaluator() {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
    }

    public double evaluateFitness(String patternString, DataStream<BaseEvent> eventStream, List<List<BaseEvent>> targetSequences) throws Exception {
        int totalTargetSequences = targetSequences.size();
        int matchedSequences = 0;

        // Generate the pattern from the string
        Pattern<BaseEvent, ?> pattern = FlinkCEPPatternGenerator.generatePattern(patternString);

        // Add watermark for timestamp management
        eventStream = eventStream.assignTimestampsAndWatermarks(
                WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> (Long) event.getField("timestamp"))
        );

        // Apply the pattern to the full event stream
        PatternStream<BaseEvent> patternStream = CEP.pattern(eventStream, pattern);

        // Extract the matches from the pattern
        DataStream<List<Tuple3<String, Integer, Long>>> resultStream = patternStream.select(new PatternSelectFunction<BaseEvent, List<Tuple3<String, Integer, Long>>>() {
            @Override
            public List<Tuple3<String, Integer, Long>> select(Map<String, List<BaseEvent>> pattern) {
                List<Tuple3<String, Integer, Long>> matchedEvents = new ArrayList<>();

                // Iterate through matched events
                for (Map.Entry<String, List<BaseEvent>> entry : pattern.entrySet()) {
                    List<BaseEvent> events = entry.getValue();
                    for (BaseEvent event : events) {
                        matchedEvents.add(new Tuple3<>(
                                (String) event.getField("name"),
                                (Integer) event.getField("value"),
                                (Long) event.getField("timestamp")
                        ));
                    }
                }
                return matchedEvents;
            }
        });

        // Execute the Flink runtime and collect the results
        List<List<Tuple3<String, Integer, Long>>> allMatches = new ArrayList<>();
        resultStream.executeAndCollect().forEachRemaining(allMatches::add);

        // Compare the matches with the target sequences
        for (List<BaseEvent> targetSequence : targetSequences) {
            List<Tuple3<String, Integer, Long>> targetTupleSequence = convertToTupleSequence(targetSequence);
            if (allMatches.contains(targetTupleSequence)) {
                matchedSequences++;
            }
        }

        // Calculate accuracy as the percentage of target sequences matched
        return (double) matchedSequences / totalTargetSequences;
    }

    private List<Tuple3<String, Integer, Long>> convertToTupleSequence(List<BaseEvent> events) {
        List<Tuple3<String, Integer, Long>> tupleSequence = new ArrayList<>();
        for (BaseEvent event : events) {
            tupleSequence.add(new Tuple3<>(
                    (String) event.getField("name"),
                    (Integer) event.getField("value"),
                    (Long) event.getField("timestamp")
            ));
        }
        return tupleSequence;
    }
}
