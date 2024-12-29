package grammar.utils;

import utils.CsvAnalyzer;
import grammar.datatypes.DataTypesEnum;
import grammar.datatypes.GrammarTypes;

import java.util.*;

import static utils.Utils.loadConfig;

public class GrammarBuilder {

    public static String buildGrammar(Map<String, DataTypesEnum> columnTypes,
                                      Map<String, Set<String>> uniqueStringValues,
                                      List<DataTypesEnum> uniqueColumnTypes,
                                      GrammarTypes grammarType,
                                      String csvFilePath) throws Exception {
        StringBuilder grammar = new StringBuilder();
        System.out.println("[GrammarBuilder] uniqueColumnTypes : "+ uniqueColumnTypes);

        // Load attributes for conditions
        Properties myConfig = loadConfig("src/main/resources/config.properties");
        String conditionAttributesConfig = myConfig.getProperty("conditionAttributes", "").trim();
        Set<String> allowedAttributes = new HashSet<>();
        if (!conditionAttributesConfig.isEmpty()) {
            allowedAttributes.addAll(Arrays.asList(conditionAttributesConfig.split(",")));
        }
        System.out.println("[GrammarBuilder] allowedAttributes : "+ allowedAttributes);

        // Max value
        final long maxBoundedValue = CsvAnalyzer.calculateDurationFromCsv(csvFilePath);
        final int maxDigits = String.valueOf(maxBoundedValue).length();

        // Define the top-level pattern structure
        grammar.append("<pattern> ::= <events> <withinClause> <key_by> | <events> <withinClause> <key_by> | <events> <withinClause> | <events>\n");

        // Define within clause (time-bound constraint)
        if (grammarType.equals(GrammarTypes.BOUNDED_DURATION) || grammarType.equals(GrammarTypes.BOUNDED_DURATION_AND_KEY_BY)) {
            System.out.println("[GrammarBuilder] maxBoundedValue : " + maxBoundedValue + ".");

            grammar.append("<withinClause> ::= <boundedDuration>\n");
            grammar.append("<boundedDuration> ::= ");

            for (int i = 1; i <= maxDigits; i++) {
                grammar.append("<digit>");
                for (int j = 1; j < i; j++) {
                    grammar.append(" <digit>");
                }
                if (i < maxDigits) {
                    grammar.append(" | ");
                }
            }
            grammar.append("\n");
        } if (grammarType.equals(GrammarTypes.UNBOUNDED)) {
            System.out.println("[GrammarBuilder] Unbounded duration.");
            grammar.append("<withinClause> ::= <iNum>\n");
        }


        // Define events sequence with strict/relaxed contiguity options
        grammar.append("<events> ::= <event> | <event> <eConcat> <events>\n");
        grammar.append("<event> ::= <identifier> <conditions> <quantifier>\n");

        // Conditions and concatenators
        grammar.append("<conditions> ::= <condition> | <condition> <cConcat> <conditions>\n");
        grammar.append("<cConcat> ::= and | or\n");

        // Event identifier and concatenators
        grammar.append("<identifier> ::= event\n");
        grammar.append("<eConcat> ::= next | followedBy | followedByAny \n");

        // Condition for selected fields
        grammar.append("<condition> ::= ");
        StringJoiner conditionJoiner = new StringJoiner(" | ");
        for (Map.Entry<String, DataTypesEnum> entry : columnTypes.entrySet()) {
            String column = entry.getKey();
            if (!column.equals("timestamp") && (allowedAttributes.isEmpty() || allowedAttributes.contains(column))) {
                // Generate conditions only for allowed attributes or all if the list is empty
                conditionJoiner.add(generateConditionForType(column, entry.getValue(), uniqueStringValues));
            }
        }
        grammar.append(conditionJoiner.toString()).append("\n");

        // key_by operator definition
        if (grammarType.equals(GrammarTypes.BOUNDED_KEY_BY) || grammarType.equals(GrammarTypes.BOUNDED_DURATION_AND_KEY_BY)){
            String keyByField = utils.Utils.getRequiredProperty(myConfig, "keyByField");
            System.out.println("[GrammarBuilder] Bounded key by:"+keyByField);
            grammar.append("<key_by> ::= "+keyByField+"\n");
        }else{
            System.out.println("[GrammarBuilder] Unbounded key by.");
            grammar.append("<key_by> ::= ");
            StringJoiner keyByJoiner = new StringJoiner(" | ");
            for (Map.Entry<String, DataTypesEnum> entry : columnTypes.entrySet()) {
                if (!entry.getKey().equals("timestamp")) {
                    // Doesn't key_by streams on timestamps
                    keyByJoiner.add(entry.getKey());
                }
            }
            grammar.append(keyByJoiner.toString()).append("\n");
        }

        // Operators for each data type
        if (uniqueColumnTypes.contains(DataTypesEnum.INT) || uniqueColumnTypes.contains(DataTypesEnum.LONG) || uniqueColumnTypes.contains(DataTypesEnum.DOUBLE)) {
            grammar.append("<opNum> ::= equal | notEqual | lt | gt\n");
        }
        if (uniqueColumnTypes.contains(DataTypesEnum.BOOLEAN)) {
            grammar.append("<opBool> ::= equal | notEqual\n");
        }
        if (uniqueColumnTypes.contains(DataTypesEnum.STRING)) {
            grammar.append("<opStr> ::= equal | notEqual\n");
        }

        // Quantifiers
        grammar.append("<quantifier> ::= oneOrMore | optional | times <greaterThanZeroNum> | range <greaterThanZeroNum> <greaterThanZeroNum>\n");

        // Number representation
        if (uniqueColumnTypes.contains(DataTypesEnum.LONG) || uniqueColumnTypes.contains(DataTypesEnum.DOUBLE)){
            grammar.append("<fNum> ::= + <digit> . <digit> <digit> E <digit> | - <digit> . <digit> <digit> E <digit> | + <digit> . <digit> <digit> E - <digit> | - <digit> . <digit> <digit> E - <digit>\n");
        }
        grammar.append("<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
        grammar.append("<greaterThanZeroDigit> ::= 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
        grammar.append("<iNum> ::= <digit> | <iNum> <digit>\n");
        grammar.append("<greaterThanZeroNum> ::= <greaterThanZeroDigit> | <greaterThanZeroNum> <digit> | <greaterThanZeroDigit> <greaterThanZeroNum>\n");

        if (columnTypes.containsValue(DataTypesEnum.BOOLEAN)) {
            grammar.append("<boolean> ::= true | false\n");
        }

        for (Map.Entry<String, Set<String>> entry : uniqueStringValues.entrySet()) {
            grammar.append("<").append(entry.getKey()).append("Value> ::= ");
            StringJoiner stringValuesJoiner = new StringJoiner(" | ");
            for (String value : entry.getValue()) {
                stringValuesJoiner.add("'" + value + "'");
            }
            grammar.append(stringValuesJoiner.toString()).append("\n");
        }

        return grammar.toString();
    }

    private static String generateConditionForType(String column, DataTypesEnum type, Map<String, Set<String>> uniqueStringValues) {
        return switch (type) {
            case INT, DOUBLE -> String.format("%s <opNum> <fNum>", column);
            case BOOLEAN -> String.format("%s <opBool> <boolean>", column);
            case STRING -> String.format("%s <opStr> <%sValue>", column, column);
            default -> throw new IllegalArgumentException("Unsupported data type: " + type);
        };
    }
}
