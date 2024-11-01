package events.source;

import events.BaseEvent;
import events.custom.LoginEvent;
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

public class FileEventSource {

    // Generate a DataStream with LoginEvent instances from a CSV file
    public static DataStream<BaseEvent> generateLoginEventDataStreamFromCSV(StreamExecutionEnvironment env, String csvFilePath) {
        List<BaseEvent> events = new ArrayList<>();
        try {
            Reader reader = new FileReader(csvFilePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()  // This line skips the header
                    .withTrim());

            for (CSVRecord record : csvParser) {
                try {
                    // Parse the timestamp, ip address, and login status from CSV
                    long timestamp = Long.parseLong(record.get("timestamp"));
                    String ipAddress = record.get("ip_address");
                    boolean successfulLogin = Boolean.parseBoolean(record.get("successful_login"));

                    // Create a LoginEvent
                    LoginEvent event = new LoginEvent(timestamp, ipAddress, successfulLogin);
                    events.add(event);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid record: " + record);
                }
            }
            csvParser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Generated " + events.size() + " login events from CSV.");

        return env.fromCollection(events)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
    }
}
