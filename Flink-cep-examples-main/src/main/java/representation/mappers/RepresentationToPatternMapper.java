// RepresentationToPatternMapper.java
package representation.mappers;

import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import representation.PatternRepresentation;

import java.time.Duration;
import java.util.List;

public class RepresentationToPatternMapper<E> {

    public Pattern<E, ?> convert(PatternRepresentation representation) {
        List<PatternRepresentation.Event> events = representation.events();
        Pattern<E, E> flinkPattern = null;

        for (int i = 0; i < events.size(); i++) {
            PatternRepresentation.Event event = events.get(i);
            Pattern<E, E> newPattern = createPatternForEvent(event);

            if (flinkPattern == null) {
                flinkPattern = newPattern;  // Set the first event as the starting point
            } else {
                // Link patterns using concatenators (e.g., next, followedBy)
                PatternRepresentation.Event.Concatenator concatenator = events.get(i - 1).concatenator();
                if (concatenator != null) {
                    flinkPattern = switch (concatenator) {
                        case NEXT -> flinkPattern.next(newPattern);
                        case FOLLOWED_BY -> flinkPattern.followedBy(newPattern);
                        case FOLLOWED_BY_ANY -> flinkPattern.followedByAny(newPattern);
                    };
                } else {
                    // Handle cases where concatenator is null (e.g., first event)
                    flinkPattern = flinkPattern.next(newPattern);
                }

            }
        }

        // Apply within clause (time window) if specified
        if (representation.withinClause() != null) {
            flinkPattern = flinkPattern.within(Duration.ofSeconds((long) representation.withinClause().duration()));
        }

        return flinkPattern;
    }

    private Pattern<E, E> createPatternForEvent(PatternRepresentation.Event event) {
        Pattern<E, E> pattern = Pattern.<E>begin(event.identifier());

        // Handle quantifiers such as oneOrMore, optional, etc.
        if (event.quantifier() instanceof PatternRepresentation.Quantifier.ParamFree quantifier) {
            pattern = switch (quantifier) {
                case ONE_OR_MORE -> pattern.oneOrMore();
                case OPTIONAL -> pattern.optional();
            };
        } else if (event.quantifier() instanceof PatternRepresentation.Quantifier.NTimes nTimes) {
            pattern = pattern.times(nTimes.n());
        }

        // Attach conditions using SimpleCondition
        for (PatternRepresentation.Condition condition : event.conditions()) {
            pattern = pattern.where(new SimpleEventCondition<>(condition));
        }

        return pattern;
    }

    // Inner class for handling SimpleCondition
    private static class SimpleEventCondition<E> extends SimpleCondition<E> {
        private final PatternRepresentation.Condition condition;

        public SimpleEventCondition(PatternRepresentation.Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean filter(E value) throws Exception {
            if (value instanceof java.util.Map) {
                java.util.Map<String, Float> map = (java.util.Map<String, Float>) value;
                Float variableValue = map.get(condition.variable());


                // Check if variable exists
                if (variableValue == null) {
                    return false;
                }

                // Perform condition check
                boolean result = switch (condition.operator()) {
                    case EQUAL -> variableValue.equals(condition.value());
                    case NOT_EQUAL -> !variableValue.equals(condition.value());
                    case LESS_THAN -> variableValue < condition.value();
                    case GREATER_THAN -> variableValue > condition.value();
                };

                return result;
            }

            System.out.println("Event type not recognized as map, condition skipped: " + value);
            return false;
        }
    }
}
