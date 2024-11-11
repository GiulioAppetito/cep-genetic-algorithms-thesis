package utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class GrammarGenerator {

    //Generates a grammar based on the structure of a CSV file and writes it to a .bnf file.
    public static void generateGrammar(String csvFilePath, String grammarFilePath) throws IOException {
        // Automatically infer column types and unique values from the CSV file
        Map<String, String> columnTypes = getColumnTypesFromCSV(csvFilePath);
        Map<String, Set<String>> uniqueStringValues = new HashMap<>();
        inferUniqueStringValues(csvFilePath, columnTypes, uniqueStringValues);

        StringBuilder grammar = new StringBuilder();

        // Root pattern structure
        grammar.append("<pattern> ::= <events> <withinClause> | <events>\n");
        grammar.append("<withinClause> ::= <iNum>\n");
        grammar.append("<events> ::= <event> | <event> <eConcat> <events>\n");
        grammar.append("<event> ::= <identifier> <conditions> <quantifier>\n");
        grammar.append("<conditions> ::= <condition> | <condition> <cConcat> <conditions>\n");
        grammar.append("<eConcat> ::= next | followedBy | followedByAny\n");

        // Identifiers for event types
        grammar.append("<identifier> ::= event\n");

        // Logical operators for conditions
        grammar.append("<cConcat> ::= and | or\n");

        // Generate <condition> based on column types
        grammar.append("<condition> ::= ");
        StringJoiner conditionJoiner = new StringJoiner(" | ");
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            String column = entry.getKey();
            String type = entry.getValue();
            conditionJoiner.add(generateConditionForType(column, type, uniqueStringValues));
        }
        grammar.append(conditionJoiner.toString()).append("\n");

        // Add operator definitions
        grammar.append("<opNum> ::= equal | notEqual | lt | gt\n");
        grammar.append("<opStr> ::= equal | notEqual \n");
        grammar.append("<opBool> ::= equal | notEqual \n");

        // Quantifiers for event occurrences
        grammar.append("<quantifier> ::= oneOrMore | optional | <iNum>\n");

        // Numeric types
        grammar.append("<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
        grammar.append("<iNum> ::= <digit> | <iNum> <digit>\n");

        // Floating-point format
        grammar.append("<fNum> ::= + <digit> . <digit> <digit> E <digit> | - <digit> . <digit> <digit> E <digit> | + <digit> . <digit> <digit> E - <digit> | - <digit> . <digit> <digit> E - <digit>\n");

        // Boolean options
        if (columnTypes.containsValue("boolean")) {
            grammar.append("<boolean> ::= true | false\n");
        }

        // Add unique string values for each string column
        for (Map.Entry<String, Set<String>> entry : uniqueStringValues.entrySet()) {
            String column = entry.getKey();
            Set<String> values = entry.getValue();
            grammar.append("<").append(column).append("Value> ::= ");
            StringJoiner stringValuesJoiner = new StringJoiner(" | ");
            for (String value : values) {
                stringValuesJoiner.add("'" + value + "'");
            }
            grammar.append(stringValuesJoiner.toString()).append("\n");
        }

        // Write grammar to file
        try (FileWriter writer = new FileWriter(grammarFilePath)) {
            writer.write(grammar.toString());
            System.out.println("Grammar generated and saved to " + grammarFilePath);
        }
    }

    //Read the CSV file and infer data types of each column
    public static Map<String, String> getColumnTypesFromCSV(String csvFilePath) throws IOException {
        Map<String, String> columnTypes = new HashMap<>();
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Iterate through each record in the CSV file
            for (CSVRecord record : csvParser) {
                // For each column in the current record
                for (String column : record.toMap().keySet()) {
                    String value = record.get(column);
                    String currentType = inferType(value);

                    // Add or update the type for each column based on its values
                    columnTypes.merge(column, currentType, (existingType, newType) -> {
                        // If existing type is 'int' but new type is 'float', upgrade to 'float'
                        if (existingType.equals("int") && newType.equals("float")) {
                            return "float";
                        }
                        // If types conflict and cannot be combined, set to 'string' as fallback
                        return existingType.equals(newType) ? existingType : "string";
                    });
                }
            }
        }
        return columnTypes;
    }


    //Infers the types of columns and collects unique values for string columns in a CSV file.
    private static void inferUniqueStringValues(String csvFilePath, Map<String, String> columnTypes, Map<String, Set<String>> uniqueStringValues) throws IOException {
        try (Reader reader = new FileReader(csvFilePath); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                for (String column : record.toMap().keySet()) {
                    String value = record.get(column);

                    // Collect unique values for string columns
                    if ("string".equals(columnTypes.get(column))) {
                        uniqueStringValues.computeIfAbsent(column, k -> new HashSet<>()).add(value);
                    }
                }
            }
        }
    }

    //Generates a condition clause for a specific type, allowing only valid operators for that type.
    private static String generateConditionForType(String column, String type, Map<String, Set<String>> uniqueStringValues) {
        return switch (type) {
            case "int", "float" -> String.format("%s <opNum> <fNum>", column);
            case "boolean" -> String.format("%s <opBool> <boolean>", column);
            case "string" -> String.format("%s <opStr> <%sValue>", column, column);
            default -> throw new IllegalArgumentException("Unsupported data type: " + type);
        };
    }

    //Infers the data type of a single CSV field value.
    private static String inferType(String value) {
        if (value.matches("-?\\d+")) {
            return "int";
        } else if (value.matches("-?\\d*\\.\\d+([eE][-+]?\\d+)?")) {
            return "float";
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "boolean";
        } else {
            return "string";
        }
    }

    public static void main(String[] args) {
        String csvFilePath = "Flink-cep-examples-main/src/main/resources/datasets/numericData.csv";  // Specify your CSV file path
        String grammarFilePath = "Flink-cep-examples-main/src/main/resources/grammars/generated/generatedGrammar.bnf";  // Specify where to save the grammar file

        try {
            generateGrammar(csvFilePath, grammarFilePath);
        } catch (IOException e) {
            System.err.println("An error occurred while generating the grammar: " + e.getMessage());
        }
    }
}
