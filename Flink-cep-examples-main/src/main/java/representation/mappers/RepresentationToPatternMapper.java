package representation.mappers;

import events.BaseEvent;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import representation.PatternRepresentation;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class RepresentationToPatternMapper<E extends BaseEvent> {

    public Pattern<E, ?> convert(PatternRepresentation representation) {
        List<PatternRepresentation.Event> events = representation.events();
        Pattern<E, E> flinkPattern = null;

        for (int i = 0; i < events.size(); i++) {
            PatternRepresentation.Event event = events.get(i);
            Pattern<E, E> newPattern = createPatternForEvent(event);

            if (flinkPattern == null) {
                // Set the first event as the starting point
                flinkPattern = newPattern;
            } else {
                PatternRepresentation.Event.Concatenator concatenator = events.get(i - 1).concatenator();
                if (concatenator != null) {
                    flinkPattern = switch (concatenator) {
                        case NEXT -> flinkPattern.next(newPattern);
                        case FOLLOWED_BY -> flinkPattern.followedBy(newPattern);
                        case FOLLOWED_BY_ANY -> flinkPattern.followedByAny(newPattern);
                    };
                } else {
                    flinkPattern = flinkPattern.next(newPattern);
                }
            }
        }

        // Apply within clause if specified
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
    private static class SimpleEventCondition<E extends BaseEvent> extends SimpleCondition<E> {
        private final PatternRepresentation.Condition condition;

        public SimpleEventCondition(PatternRepresentation.Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean filter(E value) throws Exception {
            // Use the toMap() method from BaseEvent to get the field values
            Map<String, Object> eventMap = value.toMap();

            // Get the value of the specified variable from the map
            Object fieldValue = eventMap.get(condition.variable());

            if (fieldValue instanceof Float) {
                Float variableValue = (Float) fieldValue;

                // Condition check
                return switch (condition.operator()) {
                    case EQUAL -> variableValue.equals(condition.value());
                    case NOT_EQUAL -> !variableValue.equals(condition.value());
                    case LESS_THAN -> variableValue < condition.value();
                    case GREATER_THAN -> variableValue > condition.value();
                };
            } else {
                System.out.println("Variable is not a float type or not found: " + fieldValue);
                return false;
            }
        }
    }
}