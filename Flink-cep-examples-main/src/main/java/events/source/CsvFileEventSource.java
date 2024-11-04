package events.source;

import events.engineering.BaseEvent;
import events.engineering.GenericEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import utils.GrammarGenerator;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvFileEventSource {

    public static DataStream<BaseEvent> generateEventDataStreamFromCSV(StreamExecutionEnvironment env, String csvFilePath) {
        List<BaseEvent> events = new ArrayList<>();

        try {
            Map<String, String> columnTypes = GrammarGenerator.getColumnTypesFromCSV(csvFilePath);

            try (Reader reader = new FileReader(csvFilePath);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : csvParser) {
                    Long timestamp;
                    try {
                        timestamp = Long.parseLong(record.get("timestamp"));
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse 'timestamp' as Long with value: " + record.get("timestamp"));
                        continue;
                    }

                    GenericEvent event = new GenericEvent(timestamp);

                    // Parse other attributes without "timestamp"
                    for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
                        String column = entry.getKey();

                        // Skip the "timestamp" field since it's already parsed
                        if ("timestamp".equals(column)) {
                            continue;
                        }

                        String type = entry.getValue();
                        String value = record.get(column);

                        try {
                            switch (type) {
                                case "int" -> event.setAttribute(column, Integer.parseInt(value));
                                case "float" -> event.setAttribute(column, Float.parseFloat(value));
                                case "long" -> event.setAttribute(column, Long.parseLong(value));
                                case "boolean" -> event.setAttribute(column, Boolean.parseBoolean(value));
                                case "string" -> event.setAttribute(column, value);
                                default -> throw new IllegalArgumentException("Unsupported data type: " + type);
                            }
                        } catch (NumberFormatException nfe) {
                            System.err.println("Failed to parse attribute " + column + " as " + type + " with value " + value);
                        }
                    }
                    events.add(event);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Generated " + events.size() + " events from CSV.");

        if (events.isEmpty()) {
            throw new IllegalArgumentException("Event list is empty. Ensure the CSV file is not empty and has correct data.");
        }

        return env.fromCollection(events)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
    }
}
