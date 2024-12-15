package events.factory;

import events.BaseEvent;
import events.GenericEvent;
import grammar.utils.CSVTypesExtractor;
import grammar.datatypes.DataTypesEnum;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class DataStreamFactory {

    public static DataStream<BaseEvent> createDataStream(StreamExecutionEnvironment env, String csvFilePath) {
        List<BaseEvent> events = new ArrayList<>();
        try {
            Set<String> allowedAttributes = new HashSet<>(); // Se non vuoi filtri, usa un Set vuoto
            Map<String, DataTypesEnum> columnTypes = CSVTypesExtractor.getColumnTypesFromCSV(csvFilePath, allowedAttributes);

            try (Reader reader = new FileReader(csvFilePath);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : csvParser) {
                    try {
                        Long timestamp = Long.parseLong(record.get("timestamp"));
                        GenericEvent event = new GenericEvent(timestamp);
                        for (Map.Entry<String, DataTypesEnum> entry : columnTypes.entrySet()) {
                            String column = entry.getKey();
                            if ("timestamp".equals(column)) continue;
                            String value = record.get(column);
                            switch (entry.getValue()) {
                                case INT -> event.setAttribute(column, Integer.parseInt(value));
                                case DOUBLE -> event.setAttribute(column, Double.parseDouble(value));
                                case LONG -> event.setAttribute(column, Long.parseLong(value));
                                case BOOLEAN -> event.setAttribute(column, Boolean.parseBoolean(value));
                                case STRING -> event.setAttribute(column, value);
                            }
                        }
                        events.add(event);
                    } catch (Exception e) {
                        System.err.println("Error parsing record: " + record + " - " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return env.fromCollection(events)
                .assignTimestampsAndWatermarks(WatermarkStrategy.<BaseEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, timestamp) -> event.getTimestamp()));
    }

}
