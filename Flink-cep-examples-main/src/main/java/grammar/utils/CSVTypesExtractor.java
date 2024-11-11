package grammar.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSVTypesExtractor {

    public static Map<String, String> getColumnTypesFromCSV(String csvFilePath) throws IOException {
        Map<String, String> columnTypes = new HashMap<>();
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                for (String column : record.toMap().keySet()) {
                    String value = record.get(column);
                    String currentType = inferType(value);
                    columnTypes.merge(column, currentType, (existingType, newType) -> {
                        if (existingType.equals("int") && newType.equals("float")) {
                            return "float";
                        }
                        return existingType.equals(newType) ? existingType : "string";
                    });
                }
            }
        }
        return columnTypes;
    }

    public static Map<String, Set<String>> inferUniqueStringValues(String csvFilePath, Map<String, String> columnTypes) throws IOException {
        Map<String, Set<String>> uniqueStringValues = new HashMap<>();
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                for (String column : record.toMap().keySet()) {
                    String value = record.get(column);
                    if ("string".equals(columnTypes.get(column))) {
                        uniqueStringValues.computeIfAbsent(column, k -> new HashSet<>()).add(value);
                    }
                }
            }
        }
        return uniqueStringValues;
    }

    private static String inferType(String value) {
        if (value.matches("-?\\d+")) return "int";
        else if (value.matches("-?\\d*\\.\\d+([eE][-+]?\\d+)?")) return "float";
        else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) return "boolean";
        else return "string";
    }
}
