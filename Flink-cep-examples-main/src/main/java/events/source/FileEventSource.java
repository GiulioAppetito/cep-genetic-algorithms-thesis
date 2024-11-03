package events.source;

import events.BaseEvent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileEventSource {

    public static DataStream<BaseEvent> generateEventDataStreamFromCSV(StreamExecutionEnvironment env, String csvFilePath, Map<String, String> columnTypes) {
        List<BaseEvent> events = new ArrayList<>();
        try (Reader reader = new FileReader(csvFilePath); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                BaseEvent event = new GenericEvent(Long.parseLong(record.get("timestamp")));

                for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
                    String column = entry.getKey();
                    String type = entry.getValue();
                    String value = record.get(column);

                    switch (type) {
                        case "int" -> event.setAttribute(column, Integer.parseInt(value));
                        case "float" -> event.setAttribute(column, Float.parseFloat(value));
                        case "boolean" -> event.setAttribute(column, Boolean.parseBoolean(value));
                        case "string" -> event.setAttribute(column, value);
                        default -> throw new IllegalArgumentException("Unsupported data type: " + type);
                    }
                }
                events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Generated " + events.size() + " events from CSV.");

        return env.fromCollection(events)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
    }
}
