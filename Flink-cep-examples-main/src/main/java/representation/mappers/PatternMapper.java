package representation.mappers;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.PatternRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PatternMapper implements Function<Tree<String>, PatternRepresentation> {

    @Override
    public PatternRepresentation apply(Tree<String> tree) {
        List<PatternRepresentation.SingleEvent> events = new ArrayList<>();
        PatternRepresentation.WithinClause withinClause = null;

        // Process main nodes: "<events>" and "<withinClause>"
        for (Tree<String> child : tree) {
            switch (child.content()) {
                case "<events>":
                    events = parseEvents(child);
                    break;
                case "<withinClause>":
                    withinClause = parseWithinClause(child);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected node: " + child.content());
            }
        }
        return new PatternRepresentation(events, withinClause);
    }

    private List<PatternRepresentation.SingleEvent> parseEvents(Tree<String> eventsNode) {
        List<PatternRepresentation.SingleEvent> events = new ArrayList<>();

        for (Tree<String> eventNode : eventsNode) {
            if ("<event>".equals(eventNode.content())) {
                events.add(parseSingleEvent(eventNode));
            }
        }
        return events;
    }

    private PatternRepresentation.SingleEvent parseSingleEvent(Tree<String> eventNode) {
        PatternRepresentation.SingleEvent.Concatenator concatenator = null;
        String identifier = null;
        List<PatternRepresentation.Condition> conditions = new ArrayList<>();
        PatternRepresentation.Quantifier quantifier = null;

        for (Tree<String> child : eventNode) {
            switch (child.content()) {
                case "<eConcat>":
                    concatenator = parseConcatenator(child);
                    break;
                case "<identifier>":
                    identifier = child.visitLeaves().get(0);
                    break;
                case "<conditions>":
                    conditions = parseConditions(child);
                    break;
                case "<quantifier>":
                    quantifier = parseQuantifier(child);
                    break;
            }
        }
        return new PatternRepresentation.SingleEvent(concatenator, identifier, conditions, quantifier);
    }

    private PatternRepresentation.SingleEvent.Concatenator parseConcatenator(Tree<String> concatNode) {
        String value = concatNode.visitLeaves().get(0);
        return switch (value) {
            case "next" -> PatternRepresentation.SingleEvent.Concatenator.NEXT;
            case "followedBy" -> PatternRepresentation.SingleEvent.Concatenator.FOLLOWED_BY;
            case "followedByAny" -> PatternRepresentation.SingleEvent.Concatenator.FOLLOWED_BY_ANY;
            default -> throw new IllegalArgumentException("Unknown concatenator: " + value);
        };
    }

    private List<PatternRepresentation.Condition> parseConditions(Tree<String> conditionsNode) {
        List<PatternRepresentation.Condition> conditions = new ArrayList<>();

        for (Tree<String> conditionNode : conditionsNode) {
            if ("<condition>".equals(conditionNode.content())) {
                conditions.add(parseCondition(conditionNode));
            }
        }
        return conditions;
    }

    private PatternRepresentation.Condition parseCondition(Tree<String> conditionNode) {
        PatternRepresentation.Condition.Concatenator concat = null;
        String variable = null;
        PatternRepresentation.Condition.Operator operator = null;
        float value = 0.0f;

        for (Tree<String> child : conditionNode) {
            switch (child.content()) {
                case "<cConcat>":
                    concat = parseConditionConcatenator(child);
                    break;
                case "<var>":
                    variable = child.visitLeaves().get(0);
                    break;
                case "<op>":
                    operator = parseOperator(child);
                    break;
                case "<fNum>":
                    value = parseFNum(child);
                    break;
            }
        }
        return new PatternRepresentation.Condition(concat, variable, operator, value);
    }

    private PatternRepresentation.Condition.Concatenator parseConditionConcatenator(Tree<String> concatNode) {
        String value = concatNode.visitLeaves().get(0);
        return switch (value) {
            case "and" -> PatternRepresentation.Condition.Concatenator.AND;
            case "or" -> PatternRepresentation.Condition.Concatenator.OR;
            default -> throw new IllegalArgumentException("Unknown condition concatenator: " + value);
        };
    }

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

    private float parseFNum(Tree<String> fNumNode) {
        StringBuilder fNumStr = new StringBuilder();

        for (Tree<String> digitNode : fNumNode.leaves()) {
            String content = digitNode.content();
            if (content.matches("[0-9.+-E]")) {
                fNumStr.append(content);
            }
        }

        try {
            return Float.parseFloat(fNumStr.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse floating number from string: " + fNumStr, e);
        }
    }

    private PatternRepresentation.Quantifier parseQuantifier(Tree<String> quantifierNode) {
        Tree<String> quantNode = quantifierNode.child(0);
        if ("oneOrMore".equals(quantNode.content())) {
            return PatternRepresentation.Quantifier.ParamFree.ONE_OR_MORE;
        } else if ("optional".equals(quantNode.content())) {
            return PatternRepresentation.Quantifier.ParamFree.OPTIONAL;
        } else if ("<iNum>".equals(quantNode.content())) {
            int n = Integer.parseInt(quantNode.visitLeaves().get(0));
            return new PatternRepresentation.Quantifier.NTimes(n);
        } else {
            throw new IllegalArgumentException("Unknown quantifier: " + quantNode.content());
        }
    }

    private PatternRepresentation.WithinClause parseWithinClause(Tree<String> withinClauseNode) {
        Tree<String> fNumNode = withinClauseNode.child(0);
        float duration = parseFNum(fNumNode);
        return new PatternRepresentation.WithinClause(duration);
    }
}
