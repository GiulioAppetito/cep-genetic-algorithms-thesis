package cep;

import events.engineering.BaseEvent;
import events.source.CsvFileEventSource;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CEPTargetPatternFactory {

    private static final String targetDatasetPath = "Flink-cep-examples-main/src/main/resources/datasets/target/targetDataset.csv";

    public static List<Pattern<BaseEvent, ?>> createTargetPatterns() {
        List<Pattern<BaseEvent, ?>> targetPatterns = new ArrayList<>();

        // Pattern 1: Successful login event from any IP
        Pattern<BaseEvent, BaseEvent> pattern1 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern1);

        // Pattern 2: Two consecutive successful login events from the same IP address
        Pattern<BaseEvent, BaseEvent> pattern2 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object ipAddress = event.toMap().get("ip_address");
                        Object successfulLogin = event.toMap().get("successful_login");
                        return "129.16.0.5".equals(ipAddress) &&
                                successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .next("event2")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern2);

        // Pattern 3: Sequence of three successful login events
        Pattern<BaseEvent, BaseEvent> pattern3 = Pattern
                .<BaseEvent>begin("event1")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .followedBy("event2")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                })
                .followedBy("event3")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object successfulLogin = event.toMap().get("successful_login");
                        return successfulLogin instanceof Boolean && (Boolean) successfulLogin;
                    }
                });

        targetPatterns.add(pattern3);

        return targetPatterns;
    }

    // Apply the patterns to the DataStream and save matches to a file
    public static void saveMatchesToFile(List<Pattern<BaseEvent, ?>> patterns, DataStream<BaseEvent> inputDataStream) throws Exception {
        Set<List<Map<String, Object>>> sequencesSet = new HashSet<>();

        try (FileWriter writer = new FileWriter(targetDatasetPath)) {
            for (Pattern<BaseEvent, ?> pattern : patterns) {
                PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
                DataStream<List<BaseEvent>> matchedStream = patternStream.select(new PatternToListSelectFunction());

                Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
                while (iterator.hasNext()) {
                    List<BaseEvent> eventsList = iterator.next();

                    // Convert the list of events into a list of maps for easier comparison
                    List<Map<String, Object>> sequence = new ArrayList<>();
                    for (BaseEvent event : eventsList) {
                        sequence.add(new HashMap<>(event.toMap()));
                    }

                    sequencesSet.add(sequence);
                    System.out.println("[Target] match sequence: " + sequence);

                    // Write the sequence to the CSV file
                    writer.write(sequenceToCsvLine(sequence) + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sequenceToCsvLine(List<Map<String, Object>> sequence) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> map : sequence) {
            builder.append(map.toString().replace(",", ";")).append("|");
        }
        return builder.toString();
    }

    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<BaseEvent>> {
        @Override
        public List<BaseEvent> select(Map<String, List<BaseEvent>> match) {
            List<BaseEvent> collectedEvents = new ArrayList<>();
            match.values().forEach(collectedEvents::addAll);
            return collectedEvents;
        }
    }

    public static void main(String[] args) throws Exception {
        List<Pattern<BaseEvent, ?>> targetPatterns = createTargetPatterns();

        String datasetDirPath = "Flink-cep-examples-main/src/main/resources/datasets/sources/";
        String csvFileName = "athena-sshd-processed-simple.csv";
        String csvFilePath = datasetDirPath + csvFileName;

        // Set up Flink environment and load events from CSV
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<BaseEvent> eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

        saveMatchesToFile(targetPatterns, eventStream);
    }
}
