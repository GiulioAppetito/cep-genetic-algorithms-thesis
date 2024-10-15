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

        // Creiamo il pattern a partire dalla sequenza di eventi
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
        // First event in the pattern
        FlinkCEPGrammarParser.EventContext firstEventContext = ctx.event(0);
        String firstEventName = firstEventContext.IDENTIFIER().getText();
        Pattern<Object, Object> pattern = Pattern.begin(firstEventName);

        // Conditions
        if (firstEventContext.condition() != null) {
            pattern = applyCondition(pattern, firstEventContext.condition());
        }

        // Quantifiers for first event
        if (firstEventContext.quantifier() != null) {
            pattern = applyQuantifier(pattern, firstEventContext.quantifier());
        }
        // Iterate on eventual other events
        for (int i = 1; i < ctx.event().size(); i++) {
            String op = ctx.binaryOp(i - 1).getText();
            FlinkCEPGrammarParser.EventContext nextEventContext = ctx.event(i);
            String nextEventName = nextEventContext.IDENTIFIER().getText();

            Pattern<Object, Object> nextEventPattern = Pattern.begin(nextEventName);

            // Conditions for other events
            if (nextEventContext.condition() != null) {
                nextEventPattern = applyCondition(nextEventPattern, nextEventContext.condition());
            }

            // Quantifiers for other events
            if (nextEventContext.quantifier() != null) {
                nextEventPattern = applyQuantifier(nextEventPattern, nextEventContext.quantifier());
            }

            // Binary operator between event and its predecessor
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
        String conditionVar = ctx.conditionExpression().conditionAtom().variable().getText();
        String conditionValue = ctx.conditionExpression().conditionAtom().value().getText();
        String relationalOp = ctx.conditionExpression().conditionAtom().relationalOp().getText();

        return pattern.where(new SimpleCondition<Object>() {
            @Override
            public boolean filter(Object event) {
                if (event instanceof Event) {
                    Event e = (Event) event;
                    Object fieldValue = e.getValue(); // Supponiamo che Event abbia un metodo getFieldValue

                    if (fieldValue instanceof Integer) {
                        int intValue = (Integer) fieldValue;
                        switch (relationalOp) {
                            case ">":
                                return intValue > Integer.parseInt(conditionValue);
                            case "<":
                                return intValue < Integer.parseInt(conditionValue);
                            case "==":
                                return intValue == Integer.parseInt(conditionValue);
                            case "!=":
                                return intValue != Integer.parseInt(conditionValue);
                            case ">=":
                                return intValue >= Integer.parseInt(conditionValue);
                            case "<=":
                                return intValue <= Integer.parseInt(conditionValue);
                            default:
                                throw new IllegalArgumentException("Unsupported relational operation: " + relationalOp);
                        }
                    }
                }
                return false;
            }
        });
    }

    private Pattern<Object, Object> applyQuantifier(Pattern<Object, Object> pattern, FlinkCEPGrammarParser.QuantifierContext ctx) {
        if (ctx.INT() != null) {
            int times = Integer.parseInt(ctx.INT().getText());
            return pattern.times(times);
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
