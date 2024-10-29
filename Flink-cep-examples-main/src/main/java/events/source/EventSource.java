package events.source;

import events.BaseEvent;
import events.custom.ExampleEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventSource {

    // Generate a DataStream with a specified number of ExampleEvent instances with random values
    public static DataStream<BaseEvent> generateHardcodedEventDataStream(StreamExecutionEnvironment env, int numberOfEvents) {
        List<BaseEvent> events = new ArrayList<>();
        Random random = new Random();

        // Adding random ExampleEvent instances
        for (int i = 0; i < numberOfEvents; i++) {
            Long timestamp = System.currentTimeMillis() + i;

            // Generate values both positive and negative with very large numbers
            double v1 = (random.nextFloat() - 0.5f) * 2 * random.nextInt(1_000_000); // Values between -1_000_000 and +1_000_000
            double v2 = (random.nextFloat() - 0.5f) * 2 * random.nextInt(1_000_000); // Values between -1_000_000 and +1_000_000
            double v3 = (random.nextFloat() - 0.5f) * 2 * random.nextInt(1_000_000); // Values between -1_000_000 and +1_000_000
            double v4 = (random.nextFloat() - 0.5f) * 2 * random.nextInt(1_000_000); // Values between -1_000_000 and +1_000_000
            double v5 = (random.nextFloat() - 0.5f) * 2 * random.nextInt(1_000_000); // Values between -1_000_000 and +1_000_000

            // Scaling to create very large positive or negative values
            v1 *= Math.pow(10, random.nextInt(3));
            v2 *= Math.pow(10, random.nextInt(3));
            v3 *= Math.pow(10, random.nextInt(3));
            v4 *= Math.pow(10, random.nextInt(3));
            v5 *= Math.pow(10, random.nextInt(3));

            events.add(new ExampleEvent(timestamp, (float) v1, (float) v2, (float) v3, (float) v4, (float) v5));
        }

        // Print the number of events generated
        System.out.println("Generated " + events.size() + " random events.");

        return env.fromCollection(events)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
    }
}
