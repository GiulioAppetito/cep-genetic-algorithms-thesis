package representation.mappers;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.PatternRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TreeToRepresentationMapper implements Function<Tree<String>, PatternRepresentation> {

    /**
     * <pattern> ::= <events> <withinClause> <key_by> | <events> <withinClause> <key_by> | <events> <withinClause> | <events>
     * Processes the root node to extract events and withinClause elements.
     */
    @Override
    public PatternRepresentation apply(Tree<String> tree) {
        List<PatternRepresentation.Event> events = new ArrayList<>();
        PatternRepresentation.WithinClause withinClause = null;
        PatternRepresentation.KeyByClause keyByClause = null;

        // Parse the children of the root tree node
        for (Tree<String> child : tree) {
            if ("<events>".equals(child.content())) {
                events = parseEvents(child); // Parse all events recursively
            } else if ("<withinClause>".equals(child.content())) {
                withinClause = parseWithinClause(child); // Parse within clause
            } else if ("<key_by>".equals(child.content())) {
                keyByClause = parseKeyByClause(child);
            }
        }

        // Log for debugging
        System.out.println("Parsed Events: " + events);
        System.out.println("Parsed Within Clause: " + withinClause);

        PatternRepresentation patternRepresentation = new PatternRepresentation(events, withinClause, keyByClause);

        // Validate the integrity between the tree and the PatternRepresentation
        validatePatternRepresentation(tree, patternRepresentation);

        return patternRepresentation;
    }

    /**
     * <events> ::= <event> | <event> <eConcat> <events>
     * Recursively parses a list of events and manages concatenation between events.
     */
    private List<PatternRepresentation.Event> parseEvents(Tree<String> eventsNode) {
        List<PatternRepresentation.Event> events = new ArrayList<>();

        if (eventsNode.nChildren() == 0) {
            return events;  // Base case for empty node.
        }

        // Parse the first <event>
        Tree<String> firstEventNode = eventsNode.child(0);
        PatternRepresentation.Event firstEvent = parseSingleEvent(firstEventNode);
        events.add(firstEvent);

        // If there are more than one children, handle <eConcat> <events> recursively.
        if (eventsNode.nChildren() > 1) {
            Tree<String> concatNode = eventsNode.child(1);  // <eConcat>
            Tree<String> remainingEventsNode = eventsNode.child(2);  // <events>

            PatternRepresentation.Event.Concatenator concatenator = parseConcatenator(concatNode);

            // Recursively parse remaining events.
            List<PatternRepresentation.Event> remainingEvents = parseEvents(remainingEventsNode);

            // Attach concatenator to the first event in the remaining list.
            if (!remainingEvents.isEmpty()) {
                PatternRepresentation.Event firstRemainingEvent = remainingEvents.get(0);
                remainingEvents.set(0, new PatternRepresentation.Event(
                        firstRemainingEvent.identifier(),
                        firstRemainingEvent.conditions(),
                        firstRemainingEvent.quantifier(),
                        concatenator
                ));
            }

            // Add remaining events to the main events list
            events.addAll(remainingEvents);
        }

        return events;
    }

    /**
     * <event> ::= <identifier> <conditions> <quantifier>
     * Parses a single event, including its identifier, conditions, and quantifier.
     */
    private PatternRepresentation.Event parseSingleEvent(Tree<String> eventNode) {
        String identifier = null;
        List<PatternRepresentation.Condition> conditions = new ArrayList<>();
        PatternRepresentation.Quantifier quantifier = null;

        for (Tree<String> child : eventNode) {
            switch (child.content()) {
                case "<identifier>":
                    identifier = child.visitLeaves().get(0);  // Get identifier for the event
                    break;
                case "<conditions>":
                    conditions = parseConditions(child);  // Parse all conditions within the event
                    break;
                case "<quantifier>":
                    quantifier = parseQuantifier(child);  // Parse the quantifier if available
                    break;
            }
        }

        if (identifier == null) {
            throw new IllegalArgumentException("Event identifier cannot be null");
        }

        return new PatternRepresentation.Event(identifier, conditions, quantifier, null);
    }

    /**
     * <conditions> ::= <condition> | <condition> <cConcat> <conditions>
     * Recursively parses a list of conditions and manages concatenation between them.
     */
    private List<PatternRepresentation.Condition> parseConditions(Tree<String> conditionsNode) {
        List<PatternRepresentation.Condition> conditions = new ArrayList<>();

        if (conditionsNode.nChildren() == 0) {
            return conditions;  // Base case for empty node
        }

        // Parse the first <condition>
        Tree<String> firstConditionNode = conditionsNode.child(0);
        PatternRepresentation.Condition firstCondition = parseCondition(firstConditionNode);
        conditions.add(firstCondition);

        // If there are more than one children, handle <cConcat> <conditions> recursively
        if (conditionsNode.nChildren() > 1) {
            Tree<String> concatNode = conditionsNode.child(1);  // <cConcat>
            Tree<String> remainingConditionsNode = conditionsNode.child(2);  // <conditions>

            PatternRepresentation.Condition.Concatenator concatenator = parseConditionConcatenator(concatNode);

            // Recursively parse remaining conditions
            List<PatternRepresentation.Condition> remainingConditions = parseConditions(remainingConditionsNode);

            // Attach concatenator to the first condition in the remaining list
            if (!remainingConditions.isEmpty()) {
                PatternRepresentation.Condition firstRemainingCondition = remainingConditions.get(0);
                remainingConditions.set(0, new PatternRepresentation.Condition(
                        firstRemainingCondition.variable(),
                        firstRemainingCondition.operator(),
                        firstRemainingCondition.value(),
                        concatenator
                ));
            }

            // Add remaining conditions to the main conditions list
            conditions.addAll(remainingConditions);
        }

        return conditions;
    }

    /**
     * Parses a single condition, including its variable, operator, and value.
     */
    private PatternRepresentation.Condition parseCondition(Tree<String> conditionNode) {
        String variable = null;
        PatternRepresentation.Condition.Operator operator = null;
        Object value = null;

        for (Tree<String> child : conditionNode) {
            switch (child.content()) {
                case "<var>":
                    variable = child.visitLeaves().get(0);
                    break;
                case "<opNum>":
                case "<opStr>":
                case "<opBool>":
                    operator = parseOperator(child);  // Operatore comune per numerico, string e booleano
                    break;
                case "<fNum>":
                    value = parseFNum(child);  // Valori numerici
                    break;
                case "<boolean>":
                    value = parseBooleanValue(child);  // Valori booleani
                    break;
                default:
                    if (child.content().contains("Value")) {
                        value = parseStringValue(child);  // Valori stringa
                    } else {
                        variable = child.visitLeaves().get(0);  // Gestisci come variabile generica
                    }
                    break;
            }
        }

        if (variable == null || operator == null) {
            throw new IllegalArgumentException("Condition must have both variable and operator defined");
        }

        return new PatternRepresentation.Condition(variable, operator, value, null);
    }

    /**
     * Parses concatenation types between events.
     */
    private PatternRepresentation.Event.Concatenator parseConcatenator(Tree<String> concatNode) {
        String value = concatNode.visitLeaves().get(0);
        return switch (value) {
            case "next" -> PatternRepresentation.Event.Concatenator.NEXT;
            case "followedBy" -> PatternRepresentation.Event.Concatenator.FOLLOWED_BY;
            case "followedByAny" -> PatternRepresentation.Event.Concatenator.FOLLOWED_BY_ANY;
            default -> throw new IllegalArgumentException("Unknown concatenator: " + value);
        };
    }

    /**
     * Parses logical concatenation operators between conditions.
     */
    private PatternRepresentation.Condition.Concatenator parseConditionConcatenator(Tree<String> concatNode) {
        String value = concatNode.visitLeaves().get(0);
        return switch (value) {
            case "and" -> PatternRepresentation.Condition.Concatenator.AND;
            case "or" -> PatternRepresentation.Condition.Concatenator.OR;
            default -> throw new IllegalArgumentException("Unknown condition concatenator: " + value);
        };
    }

    /**
     * Parses comparison operators within a condition.
     */
    private PatternRepresentation.Condition.Operator parseOperator(Tree<String> opNode) {
        String value = opNode.visitLeaves().get(0);
        return switch (value) {
            case "equal" -> PatternRepresentation.Condition.Operator.EQUAL;
            case "notEqual" -> PatternRepresentation.Condition.Operator.NOT_EQUAL;
            case "lt" -> PatternRepresentation.Condition.Operator.LESS_THAN;
            case "gt" -> PatternRepresentation.Condition.Operator.GREATER_THAN;
            default -> throw new IllegalArgumentException("Unknown operator: " + value);
        };
    }

    /**
     * Parses a floating-point number format with scientific notation.
     */
    private float parseFNum(Tree<String> fNumNode) {
        StringBuilder fNumStr = new StringBuilder();

        for (Tree<String> digitNode : fNumNode.leaves()) {
            String content = digitNode.content();
            if (content.matches("[0-9.+-E]")) {
                fNumStr.append(content);
            }
        }

        return Float.parseFloat(fNumStr.toString());
    }

    /**
     * Parses boolean values.
     */
    private boolean parseBooleanValue(Tree<String> boolNode) {
        String value = boolNode.visitLeaves().get(0);
        return Boolean.parseBoolean(value);
    }

    /**
     * Parses string values.
     */
    private String parseStringValue(Tree<String> strNode) {
        return strNode.visitLeaves().get(0);
    }

    /**
     * Parses the quantifier that defines event occurrence constraints.
     */
    private PatternRepresentation.Quantifier parseQuantifier(Tree<String> quantifierNode) {
        Tree<String> quantNode = quantifierNode.child(0);
        return switch (quantNode.content()) {
            case "oneOrMore" -> PatternRepresentation.Quantifier.ParamFree.ONE_OR_MORE;
            case "optional" -> PatternRepresentation.Quantifier.ParamFree.OPTIONAL;
            case "<iNum>" -> new PatternRepresentation.Quantifier.NTimes(
                    Integer.parseInt(quantNode.visitLeaves().get(0))
            );
            default -> throw new IllegalArgumentException("Unknown quantifier: " + quantNode.content());
        };
    }

    /**
     * Parses the time constraint applied to the event sequence.
     */
    private PatternRepresentation.WithinClause parseWithinClause(Tree<String> withinClauseNode) {
        float duration = parseFNum(withinClauseNode.child(0));
        return new PatternRepresentation.WithinClause(duration);
    }

    /**
     * Parses the key_by applied to the event sequence.
     */
    private PatternRepresentation.KeyByClause parseKeyByClause(Tree<String> child) {
        String key = child.visitLeaves().get(0);
        return new PatternRepresentation.KeyByClause(key);
    }

    /**
     * Validates the integrity between the parsed tree and the generated PatternRepresentation.
     */
    private void validatePatternRepresentation(Tree<String> tree, PatternRepresentation patternRepresentation) {
        System.out.println("Validating Pattern Representation...");
        if (patternRepresentation.events().size() != countEventNodes(tree)) {
            System.err.println("Mismatch in number of events between Tree and PatternRepresentation.");
        }
    }

    /**
     * Counts the number of <event> nodes in the tree.
     */
    private int countEventNodes(Tree<String> tree) {
        int count = 0;
        if ("<event>".equals(tree.content())) {
            count++;
        }
        for (Tree<String> child : tree) {
            count += countEventNodes(child);
        }
        return count;
    }
}
