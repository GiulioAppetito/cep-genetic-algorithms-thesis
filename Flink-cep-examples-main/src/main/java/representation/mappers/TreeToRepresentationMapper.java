package representation.mappers;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.PatternRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TreeToRepresentationMapper implements Function<Tree<String>, PatternRepresentation> {

    /**
     * <pattern> ::= <events> <withinClause> | <events>
     * Processes the root node to extract events and withinClause elements.
     */
    @Override
    public PatternRepresentation apply(Tree<String> tree) {
        List<PatternRepresentation.Event> events = new ArrayList<>();
        PatternRepresentation.WithinClause withinClause = null;

        for (Tree<String> child : tree) {
            if ("<events>".equals(child.content())) {
                events = parseEvents(child); // Parse all events recursively
            } else if ("<withinClause>".equals(child.content())) {
                withinClause = parseWithinClause(child); // Parse within clause
            }
        }
        return new PatternRepresentation(events, withinClause);
    }

    /**
     * <events> ::= <event> | <event> <eConcat> <events>
     * Recursively parses a list of events and manages concatenation between events.
     */
    private List<PatternRepresentation.Event> parseEvents(Tree<String> eventsNode) {
        List<PatternRepresentation.Event> events = new ArrayList<>();

        if (eventsNode.nChildren() == 0) {
            return events;  // Base case for an empty node.
        }

        // Parse the first <event>.
        Tree<String> firstEventNode = eventsNode.child(0);
        PatternRepresentation.Event firstEvent = parseSingleEvent(firstEventNode);
        events.add(firstEvent);

        // If there are more children, handle <eConcat> <events> recursively.
        if (eventsNode.nChildren() > 1) {
            Tree<String> concatNode = eventsNode.child(1);  // <eConcat>
            Tree<String> remainingEventsNode = eventsNode.child(2);  // <events>

            PatternRepresentation.Event.Concatenator concatenator = parseConcatenator(concatNode);

            // Recursively parse the remaining events.
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

            // Add remaining events to the main events list.
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
                    identifier = child.visitLeaves().get(0);  // Get the identifier for the event
                    break;
                case "<conditions>":
                    conditions = parseConditions(child);  // Parse all conditions within the event
                    break;
                case "<quantifier>":
                    quantifier = parseQuantifier(child);  // Parse the quantifier if available
                    break;
            }
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
            return conditions;  // Base case for an empty node
        }

        // Parse the first <condition>
        Tree<String> firstConditionNode = conditionsNode.child(0);
        PatternRepresentation.Condition firstCondition = parseCondition(firstConditionNode);
        conditions.add(firstCondition);

        // If there are more children, handle <cConcat> <conditions> recursively
        if (conditionsNode.nChildren() > 1) {
            Tree<String> concatNode = conditionsNode.child(1);  // <cConcat>
            Tree<String> remainingConditionsNode = conditionsNode.child(2);  // <conditions>

            PatternRepresentation.Condition.Concatenator concatenator = parseConditionConcatenator(concatNode);

            // Recursively parse the remaining conditions
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
     * <eConcat> ::= next | followedBy | followedByAny
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
     * <condition> ::= <var> <op> <boolean>
     * Parses a single condition, including its variable, operator, and boolean value.
     */
    private PatternRepresentation.Condition parseCondition(Tree<String> conditionNode) {
        String variable = null;
        PatternRepresentation.Condition.Operator operator = null;
        boolean value = false;  // Changed to boolean

        for (Tree<String> child : conditionNode) {
            switch (child.content()) {
                case "<var>":
                    variable = child.visitLeaves().get(0);
                    break;
                case "<op>":
                    operator = parseOperator(child);
                    break;
                case "<boolean>":  // Changed to <boolean>
                    value = parseBoolean(child);
                    break;
            }
        }
        return new PatternRepresentation.Condition(variable, operator, value, null);
    }

    /**
     * Parses a boolean value from <boolean> node.
     */
    private boolean parseBoolean(Tree<String> booleanNode) {
        String value = booleanNode.visitLeaves().get(0);
        return Boolean.parseBoolean(value);
    }

    /**
     * <cConcat> ::= and | or
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
     * <op> ::= equal | notEqual
     * Parses comparison operators within a condition.
     */
    private PatternRepresentation.Condition.Operator parseOperator(Tree<String> opNode) {
        String value = opNode.visitLeaves().get(0);
        return switch (value) {
            case "equal" -> PatternRepresentation.Condition.Operator.EQUAL;
            case "notEqual" -> PatternRepresentation.Condition.Operator.NOT_EQUAL;
            default -> throw new IllegalArgumentException("Unknown operator: " + value);
        };
    }

    /**
     * <quantifier> ::= oneOrMore | optional | <iNum>
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
     * <withinClause> ::= <iNum>
     * Parses the time constraint applied to the event sequence.
     */
    private PatternRepresentation.WithinClause parseWithinClause(Tree<String> withinClauseNode) {
        int duration = Integer.parseInt(withinClauseNode.visitLeaves().get(0));
        return new PatternRepresentation.WithinClause(duration);
    }
}
