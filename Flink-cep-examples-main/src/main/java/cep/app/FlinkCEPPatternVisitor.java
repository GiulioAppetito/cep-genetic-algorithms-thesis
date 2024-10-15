package cep.app;

import antlr.FlinkCEPGrammarBaseVisitor;
import antlr.FlinkCEPGrammarParser;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import cep.events.Event;

public class FlinkCEPPatternVisitor extends FlinkCEPGrammarBaseVisitor<Object> {

    @Override
    public Object visitPattern(FlinkCEPGrammarParser.PatternContext ctx) {
        System.out.println("Visiting pattern context...");

        Pattern<Object, Object> pattern = (Pattern<Object, Object>) visitEventSequence(ctx.eventSequence());

        if (ctx.withinClause() != null) {
            Time duration = (Time) visitWithinClause(ctx.withinClause());
            pattern = pattern.within(duration);
            System.out.println("Applied within clause: " + duration);
        }

        return pattern;
    }

    @Override
    public Object visitEventSequence(FlinkCEPGrammarParser.EventSequenceContext ctx) {
        // First event
        FlinkCEPGrammarParser.EventContext firstEventContext = ctx.event(0);
        String firstEventName = firstEventContext.IDENTIFIER().getText();
        Pattern<Object, Object> pattern = Pattern.begin(firstEventName);

        // Condition to first event
        if (firstEventContext.condition() != null) {
            pattern = applyCondition(pattern, firstEventContext.condition());
        }

        // Add the quantifier
        if (firstEventContext.quantifier() != null) {
            pattern = applyQuantifier(pattern, firstEventContext.quantifier());
        }

        // Process other events in the sequence
        for (int i = 1; i < ctx.event().size(); i++) {
            String op = ctx.binaryOp(i - 1).getText();
            FlinkCEPGrammarParser.EventContext nextEventContext = ctx.event(i);
            String nextEventName = nextEventContext.IDENTIFIER().getText();

            // Concatenate subsequent events
            Pattern<Object, Object> nextEventPattern = pattern;

            // Conditions for next events
            if (nextEventContext.condition() != null) {
                nextEventPattern = applyCondition(nextEventPattern, nextEventContext.condition());
            }

            // Quantifier for next event
            if (nextEventContext.quantifier() != null) {
                nextEventPattern = applyQuantifier(nextEventPattern, nextEventContext.quantifier());
            }

            // Apply binary operator between events
            switch (op) {
                case "next":
                    pattern = pattern.next(nextEventName);
                    break;
                case "followedBy":
                    pattern = pattern.followedBy(nextEventName);
                    break;
                case "followedByAny":
                    pattern = pattern.followedByAny(nextEventName);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported binary operation: " + op);
            }
        }

        return pattern;
    }

    private Pattern<Object, Object> applyCondition(Pattern<Object, Object> pattern, FlinkCEPGrammarParser.ConditionContext ctx) {
        SimpleCondition<Object> condition = buildComplexCondition(ctx.conditionExpression());
        return pattern.where(condition);
    }

    private SimpleCondition<Object> buildComplexCondition(FlinkCEPGrammarParser.ConditionExpressionContext ctx) {
        // Base condition
        SimpleCondition<Object> baseCondition = buildConditionAtom(ctx.conditionAtom());

        // Se esiste un operatore logico (AND/OR), crea la condizione combinata
        if (ctx.conditionOp() != null && ctx.conditionExpression() != null) {
            SimpleCondition<Object> nextCondition = buildComplexCondition(ctx.conditionExpression());
            String conditionOp = ctx.conditionOp().getText();

            if (conditionOp.equals("AND")) {
                return new ConditionAnd(baseCondition, nextCondition);
            } else if (conditionOp.equals("OR")) {
                return new ConditionOr(baseCondition, nextCondition);
            }
        }

        return baseCondition;
    }

    private SimpleCondition<Object> buildConditionAtom(FlinkCEPGrammarParser.ConditionAtomContext ctx) {
        String conditionVar = ctx.variable().getText();
        String conditionValue = ctx.value().getText();
        String relationalOp = ctx.relationalOp().getText();

        return new ConditionAtom(conditionVar, conditionValue, relationalOp);
    }

    // Atomic conditions
    static class ConditionAtom extends SimpleCondition<Object> {
        private final String conditionVar;
        private final String conditionValue;
        private final String relationalOp;

        public ConditionAtom(String conditionVar, String conditionValue, String relationalOp) {
            this.conditionVar = conditionVar;
            this.conditionValue = conditionValue;
            this.relationalOp = relationalOp;
        }

        @Override
        public boolean filter(Object event) {
            if (event instanceof Event) {
                Event e = (Event) event;
                Object fieldValue = e.getFieldValue(conditionVar);

                if (fieldValue instanceof Integer) {
                    return compareInteger((Integer) fieldValue, Integer.parseInt(conditionValue), relationalOp);
                } else if (fieldValue instanceof Float) {
                    return compareFloat((Float) fieldValue, Float.parseFloat(conditionValue), relationalOp);
                } else if (fieldValue instanceof String) {
                    return compareString((String) fieldValue, conditionValue, relationalOp);
                } else if (fieldValue instanceof Boolean) {
                    return compareBoolean((Boolean) fieldValue, Boolean.parseBoolean(conditionValue), relationalOp);
                }
            }
            return false;
        }

        private boolean compareInteger(int fieldValue, int conditionValue, String relationalOp) {
            switch (relationalOp) {
                case ">":
                    return fieldValue > conditionValue;
                case "<":
                    return fieldValue < conditionValue;
                case "==":
                    return fieldValue == conditionValue;
                case "!=":
                    return fieldValue != conditionValue;
                case ">=":
                    return fieldValue >= conditionValue;
                case "<=":
                    return fieldValue <= conditionValue;
                default:
                    throw new IllegalArgumentException("Unsupported relational operation: " + relationalOp);
            }
        }

        private boolean compareFloat(float fieldValue, float conditionValue, String relationalOp) {
            switch (relationalOp) {
                case ">":
                    return fieldValue > conditionValue;
                case "<":
                    return fieldValue < conditionValue;
                case "==":
                    return fieldValue == conditionValue;
                case "!=":
                    return fieldValue != conditionValue;
                case ">=":
                    return fieldValue >= conditionValue;
                case "<=":
                    return fieldValue <= conditionValue;
                default:
                    throw new IllegalArgumentException("Unsupported relational operation: " + relationalOp);
            }
        }

        private boolean compareString(String fieldValue, String conditionValue, String relationalOp) {
            switch (relationalOp) {
                case "==":
                    return fieldValue.equals(conditionValue);
                case "!=":
                    return !fieldValue.equals(conditionValue);
                default:
                    throw new IllegalArgumentException("Unsupported relational operation for strings: " + relationalOp);
            }
        }

        private boolean compareBoolean(boolean fieldValue, boolean conditionValue, String relationalOp) {
            switch (relationalOp) {
                case "==":
                    return fieldValue == conditionValue;
                case "!=":
                    return fieldValue != conditionValue;
                default:
                    throw new IllegalArgumentException("Unsupported relational operation for booleans: " + relationalOp);
            }
        }
    }

    // AND condition
    static class ConditionAnd extends SimpleCondition<Object> {
        private final SimpleCondition<Object> left;
        private final SimpleCondition<Object> right;

        public ConditionAnd(SimpleCondition<Object> left, SimpleCondition<Object> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean filter(Object event) throws Exception {
            return left.filter(event) && right.filter(event);
        }
    }

    // Classe statica interna per le condizioni OR
    static class ConditionOr extends SimpleCondition<Object> {
        private final SimpleCondition<Object> left;
        private final SimpleCondition<Object> right;

        public ConditionOr(SimpleCondition<Object> left, SimpleCondition<Object> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean filter(Object event) throws Exception {
            return left.filter(event) || right.filter(event);
        }
    }

    private Pattern<Object, Object> applyQuantifier(Pattern<Object, Object> pattern, FlinkCEPGrammarParser.QuantifierContext ctx) {
        if (ctx.INT() != null) {
            int times = Integer.parseInt(ctx.INT().getText());
            return pattern.times(times);
        } else if (ctx.getText().equals("oneOrMore")) {
            return pattern.oneOrMore();
        } else if (ctx.getText().equals("optional")) {
            return pattern.optional();
        }
        return pattern;
    }

    @Override
    public Object visitWithinClause(FlinkCEPGrammarParser.WithinClauseContext ctx) {
        String durationStr = ctx.DURATION().getText();
        int duration = Integer.parseInt(durationStr.substring(0, durationStr.length() - 1));
        char unit = durationStr.charAt(durationStr.length() - 1);

        switch (unit) {
            case 's': return Time.seconds(duration);
            case 'm': return Time.minutes(duration);
            case 'h': return Time.hours(duration);
            default: throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }
}
