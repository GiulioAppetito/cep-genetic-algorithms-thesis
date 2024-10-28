package cep.events.sources;

import cep.events.SimpleEvent;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class SimpleEventSource implements SourceFunction<SimpleEvent> {

    private volatile boolean isRunning = true;
    private final int maxEvents;  // Maximum number of events to emit

    // Constructor with maxEvents as a parameter
    public SimpleEventSource(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    @Override
    public void run(SourceContext<SimpleEvent> ctx) {
        Random random = new Random();
        int eventsEmitted = 0;

        while (isRunning && eventsEmitted < maxEvents) {
            // Emit a new SimpleEvent with random data
            String type = "event" + (eventsEmitted % 5);  // Example event type
            float value = random.nextFloat() * 10;

            ctx.collect(new SimpleEvent(type, value));
            eventsEmitted++;

            // Sleep briefly to avoid flooding the stream
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Stop the source once maxEvents are reached
        isRunning = false;
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
