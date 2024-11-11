package cep;

import events.BaseEvent;
import events.source.CsvFileEventSource;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.*;
import java.util.*;

public class TargetSequencesGenerator {

    private static String targetDatasetPath;

    public static List<Pattern<BaseEvent, ?>> createTargetPatterns() {
        List<Pattern<BaseEvent, ?>> targetPatterns = new ArrayList<>();

        // Pattern 1: Detect specific sensor (SENSOR_009) activation
        Pattern<BaseEvent, BaseEvent> pattern1 = Pattern
                .<BaseEvent>begin("sensor_009_activation")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object sensor_id = event.toMap().get("sensor_id");
                        return "SENSOR_009".equals(sensor_id);
                    }
                });

        // Pattern 2: Detect a high temperature event
        Pattern<BaseEvent, BaseEvent> pattern2 = Pattern
                .<BaseEvent>begin("high_temperature")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object temperature = event.toMap().get("temperature");
                        return temperature instanceof Number && ((Number) temperature).doubleValue() > 75.0;
                    }
                });

        // Pattern 3: Detect events with a specific location and alarm status on
        Pattern<BaseEvent, BaseEvent> pattern3 = Pattern
                .<BaseEvent>begin("location_and_alarm")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object location = event.toMap().get("location");
                        Object alarmStatus = event.toMap().get("alarm_status");
                        return "Zone_Alpha".equals(location) && Boolean.TRUE.equals(alarmStatus);
                    }
                });

        // Pattern 4: Detect a sequence where sensor changes location
        Pattern<BaseEvent, BaseEvent> pattern4 = Pattern
                .<BaseEvent>begin("location_change")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object sensor_id = event.toMap().get("sensor_id");
                        Object location = event.toMap().get("location");
                        return "SENSOR_007".equals(sensor_id) && "Zone_Bravo".equals(location);
                    }
                })
                .next("new_location")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object sensor_id = event.toMap().get("sensor_id");
                        Object location = event.toMap().get("location");
                        return "SENSOR_007".equals(sensor_id) && "Zone_Charlie".equals(location);
                    }
                });

        // Pattern 5: Detect sequence of high vibration followed by speed increase
        Pattern<BaseEvent, BaseEvent> pattern5 = Pattern
                .<BaseEvent>begin("high_vibration")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object vibration = event.toMap().get("vibration");
                        return vibration instanceof Number && ((Number) vibration).doubleValue() > 5.0;
                    }
                })
                .next("speed_increase")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object speed = event.toMap().get("speed");
                        return speed instanceof Number && ((Number) speed).doubleValue() > 20.0;
                    }
                });

        // Pattern 6: Detect optional event if alarm is on and temperature is high
        Pattern<BaseEvent, BaseEvent> pattern6 = Pattern
                .<BaseEvent>begin("alarm_and_temperature")
                .where(new SimpleCondition<BaseEvent>() {
                    @Override
                    public boolean filter(BaseEvent event) {
                        Object alarmStatus = event.toMap().get("alarm_status");
                        Object temperature = event.toMap().get("temperature");
                        return Boolean.TRUE.equals(alarmStatus) && temperature instanceof Number && ((Number) temperature).doubleValue() > 60.0;
                    }
                })
                .optional();

        // Add all patterns to the list
        targetPatterns.add(pattern1);
        targetPatterns.add(pattern2);
        targetPatterns.add(pattern3);
        targetPatterns.add(pattern4);
        targetPatterns.add(pattern5);
        targetPatterns.add(pattern6);

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
        // Load configuration properties from config.properties file
        Properties config = loadConfig("config.properties");

        // Read paths from the configuration
        String datasetDirPath = config.getProperty("datasetDirPath");
        String csvFileName = config.getProperty("csvFileName");
        targetDatasetPath = config.getProperty("targetDatasetPath", "Flink-cep-examples-main/src/main/resources/datasets/target/targetDataset.csv");

        String csvFilePath = datasetDirPath + csvFileName;

        // Set up Flink environment and load events from CSV
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<BaseEvent> eventStream = CsvFileEventSource.generateEventDataStreamFromCSV(env, csvFilePath);

        // Create target patterns and save matches to a file
        List<Pattern<BaseEvent, ?>> targetPatterns = createTargetPatterns();
        saveMatchesToFile(targetPatterns, eventStream);
    }

    private static Properties loadConfig(String filePath) throws Exception {
        Properties config = new Properties();
        try (InputStream input = TargetSequencesGenerator.class.getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new FileNotFoundException("Configuration file not found: " + filePath);
            }
            config.load(input);
        }
        return config;
    }
}
