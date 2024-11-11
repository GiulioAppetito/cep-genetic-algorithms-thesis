package grammar.utils;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class GrammarBuilder {

    public static String buildGrammar(Map<String, String> columnTypes, Map<String, Set<String>> uniqueStringValues) {
        StringBuilder grammar = new StringBuilder();

        grammar.append("<pattern> ::= <events> <withinClause> | <events>\n");
        grammar.append("<withinClause> ::= <iNum>\n");
        grammar.append("<events> ::= <event> | <event> <eConcat> <events>\n");
        grammar.append("<event> ::= <identifier> <conditions> <quantifier>\n");
        grammar.append("<conditions> ::= <condition> | <condition> <cConcat> <conditions>\n");
        grammar.append("<eConcat> ::= next | followedBy | followedByAny\n");

        grammar.append("<identifier> ::= event\n");
        grammar.append("<cConcat> ::= and | or\n");

        grammar.append("<condition> ::= ");
        StringJoiner conditionJoiner = new StringJoiner(" | ");
        for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
            conditionJoiner.add(generateConditionForType(entry.getKey(), entry.getValue(), uniqueStringValues));
        }
        grammar.append(conditionJoiner.toString()).append("\n");

        grammar.append("<opNum> ::= equal | notEqual | lt | gt\n");
        grammar.append("<opStr> ::= equal | notEqual\n");
        grammar.append("<opBool> ::= equal | notEqual\n");
        grammar.append("<quantifier> ::= oneOrMore | optional | <iNum>\n");
        grammar.append("<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9\n");
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
