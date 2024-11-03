package utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class GrammarGenerator {

    //Generates a grammar based on the structure of a CSV file and writes it to a .bnf file.
    public static void generateGrammar(String csvFilePath, String grammarFilePath) throws IOException {
        // Automatically infer column types and unique values from the CSV file
        Map<String, String> columnTypes = new HashMap<>();
        Map<String, Set<String>> uniqueStringValues = new HashMap<>();
        inferColumnTypesAndUniqueValues(csvFilePath, columnTypes, uniqueStringValues);

        StringBuilder grammar = new StringBuilder();

        // Root pattern structure
        grammar.append("<pattern> ::= <events> <withinClause> | <events>\n");
        grammar.append("<withinClause> ::= <iNum>\n");
        grammar.append("<events> ::= <event> | <event> <eConcat> <events>\n");
        grammar.append("<event> ::= <identifier> <conditions> <quantifier>\n");
        grammar.append("<conditions> ::= <condition> | <condition> <cConcat> <conditions>\n");
        grammar.append("<eConcat> ::= next | followedBy | followedByAny\n");

        // Identifiers for event types
        grammar.append("<identifier> ::= ");
        for (int i = 1; i <= 10; i++) {
            grammar.append("E").append(i);
            if (i < 10) {
                grammar.append(" | ");
            } else {
                grammar.append("\n");
            }
        }

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

        // Quantifiers for event occurrences
        grammar.append("<quantifier> ::= oneOrMore | optional | <iNum>\n");

        // Numeric types
        grammar.append("<iNum> ::= <digit> | <iNum> <digit>\n");
        grammar.append("<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");

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

    /**
     * Generates a condition clause for a specific type, allowing only valid operators for that type.
     * If the type is "string", it includes a dedicated placeholder for unique values in the condition.
     *
     * @param column The column name.
     * @param type The data type of the column (e.g., "int", "float", "boolean", "string").
     * @param uniqueStringValues A map containing unique values for each string column.
     * @return A string representing the condition clause for that type.
     */
    private static String generateConditionForType(String column, String type, Map<String, Set<String>> uniqueStringValues) {
        return switch (type) {
            case "int", "float" -> String.format("%s <opNum> <fNum>", column);
            case "boolean" -> String.format("%s <opBool> <boolean>", column);
            case "string" -> String.format("%s <opStr> <%sValue>", column, column);
            default -> throw new IllegalArgumentException("Unsupported data type: " + type);
        };
    }

    //Infers the types of columns and collects unique values for string columns in a CSV file.
    private static void inferColumnTypesAndUniqueValues(String csvFilePath, Map<String, String> columnTypes, Map<String, Set<String>> uniqueStringValues) throws IOException {
        try (Reader reader = new FileReader(csvFilePath); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                for (String column : record.toMap().keySet()) {
                    String value = record.get(column);
                    String inferredType = inferType(value);

                    // Add inferred type to columnTypes if not already present
                    columnTypes.putIfAbsent(column, inferredType);

                    // Collect unique values for string columns
                    if ("string".equals(inferredType)) {
                        uniqueStringValues.computeIfAbsent(column, k -> new HashSet<>()).add(value);
                    }
                }
            }
        }
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
        String csvFilePath = "Flink-cep-examples-main/src/main/resources/datasets/prova.csv";
        String grammarFilePath = "Flink-cep-examples-main/src/main/resources/grammars/generated/generatedGrammar.bnf";

        try {
            generateGrammar(csvFilePath, grammarFilePath);
        } catch (IOException e) {
            System.err.println("An error occurred while generating the grammar: " + e.getMessage());
        }
    }
}
