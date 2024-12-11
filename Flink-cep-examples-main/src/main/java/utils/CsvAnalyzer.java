package utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;

public class CsvAnalyzer {

    public static long calculateDurationFromCsv(String csvFilePath) throws Exception {
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;

        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                // Estrarre il timestamp dalla colonna
                long timestamp = Long.parseLong(record.get("timestamp"));
                // Aggiornare i valori minimo e massimo
                minTimestamp = Math.min(minTimestamp, timestamp);
                maxTimestamp = Math.max(maxTimestamp, timestamp);
            }
        }

        long duration = (maxTimestamp - minTimestamp)/1000;
        System.out.println("Duration: "+ duration + " seconds.");
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
}
