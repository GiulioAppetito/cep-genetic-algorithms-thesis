package grammar.utils;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class GrammarBuilder {

    public static String buildGrammar(Map<String, String> columnTypes, Map<String, Set<String>> uniqueStringValues) {
        StringBuilder grammar = new StringBuilder();

        // Define the top-level pattern structure
        grammar.append("<pattern> ::= <events> <withinClause> <key_by> | <events> <withinClause> <key_by> | <events> <withinClause> | <events>\n");

        // Define within clause (time-bound constraint)
        grammar.append("<withinClause> ::= <iNum>\n");

        // Define events sequence with strict/relaxed contiguity options
        grammar.append("<events> ::= <event> | <event> <eConcat> <events>\n");
        grammar.append("<event> ::= <identifier> <conditions> <quantifier>\n");

        // Conditions and concatenators
        grammar.append("<conditions> ::= <condition> | <condition> <cConcat> <conditions>\n");
        grammar.append("<cConcat> ::= and | or\n");

        // Event identifier and concatenators
        grammar.append("<identifier> ::= event\n");
        grammar.append("<eConcat> ::= next | followedBy | followedByAny | notNext | notFollowedBy\n");

        // Condition for every field
        grammar.append("<condition> ::= ");
        StringJoiner conditionJoiner = new StringJoiner(" | ");
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (!entry.getKey().equals("timestamp")){
                // Doesn't generate conditions on timestamps
                conditionJoiner.add(generateConditionForType(entry.getKey(), entry.getValue(), uniqueStringValues));
            }

        }
        grammar.append(conditionJoiner.toString()).append("\n");

        // key_by operator definition
        grammar.append("<key_by> ::= ");
        StringJoiner keyByJoiner = new StringJoiner(" | ");
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            if (!entry.getKey().equals("timestamp")) {
                // Doesn't key_by streams on timestamps
                keyByJoiner.add(entry.getKey());
            }
        }
        grammar.append(keyByJoiner.toString()).append("\n");

        // Operators for each data type
        grammar.append("<opNum> ::= equal | notEqual | lt | gt\n");
        grammar.append("<opStr> ::= equal | notEqual\n");
        grammar.append("<opBool> ::= equal | notEqual\n");

        // Quantifiers
        grammar.append("<quantifier> ::= oneOrMore | optional | <greaterThanZeroNum>\n");

        // Number representation
        grammar.append("<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
        grammar.append("<greaterThanZeroDigit> ::= 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
        grammar.append("<greaterThanZeroNum> ::= <greaterThanZeroDigit> | <greaterThanZeroNum> <digit> | <greaterThanZeroNum> <greaterThanZeroDigit>\n");
        grammar.append("<iNum> ::= <digit> | <iNum> <digit>\n");
        grammar.append("<fNum> ::= + <digit> . <digit> <digit> E <digit> | - <digit> . <digit> <digit> E <digit> | + <digit> . <digit> <digit> E - <digit> | - <digit> . <digit> <digit> E - <digit>\n");

        if (columnTypes.containsValue("boolean")) {
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

    private static String generateConditionForType(String column, String type, Map<String, Set<String>> uniqueStringValues) {
        return switch (type) {
            case "int", "float" -> String.format("%s <opNum> <fNum>", column);
            case "boolean" -> String.format("%s <opBool> <boolean>", column);
            case "string" -> String.format("%s <opStr> <%sValue>", column, column);
            default -> throw new IllegalArgumentException("Unsupported data type: " + type);
        };
    }
}
