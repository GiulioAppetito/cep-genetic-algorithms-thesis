package utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class CsvAnalyzer {

    public static long calculateDurationFromCsv(String csvFilePath) throws Exception {
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;

        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                long timestamp = Long.parseLong(record.get("timestamp"));
                minTimestamp = Math.min(minTimestamp, timestamp);
                maxTimestamp = Math.max(maxTimestamp, timestamp);
            }
        }

        long duration = (maxTimestamp - minTimestamp) / 1000;
        System.out.println("Duration: " + duration + " seconds.");
        return duration;
    }

    public static long countRowsInCsv(String csvFilePath) throws Exception {
        long rowCount = 0;

        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                rowCount++;
            }
        }

        System.out.println("Number of rows: " + rowCount);
        return rowCount;
    }

    public static Set<String> findKeyCandidatesFromCsv(String csvFilePath) throws Exception {
        Map<String, Set<String>> columnValueSets = new HashMap<>();

        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                for (String column : record.toMap().keySet()) {
                    columnValueSets.putIfAbsent(column, new HashSet<>());
                    columnValueSets.get(column).add(record.get(column));
                }
            }
        }

        // Choose attributes with more than one value
        Set<String> keyCandidates = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : columnValueSets.entrySet()) {
            if (entry.getValue().size() > 1) {
                keyCandidates.add(entry.getKey());
            }
        }

        System.out.println("KeyBy Candidates (CSV): " + keyCandidates);
        return keyCandidates;
    }

}
