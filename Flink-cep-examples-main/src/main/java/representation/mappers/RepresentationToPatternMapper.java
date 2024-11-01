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

        // Loop through all the events in the pattern representation
        for (int i = 0; i < events.size(); i++) {
            PatternRepresentation.Event event = events.get(i);
            Pattern<E, E> newPattern = createPatternForEvent(event);

            if (flinkPattern == null) {
                // Set the first event as the starting point
                flinkPattern = newPattern;
            } else {
                // Apply concatenation between the current and previous events if defined
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

    /**
     * Creates a Flink CEP pattern for a single event.
     */
    private Pattern<E, E> createPatternForEvent(PatternRepresentation.Event event) {
        // Start by defining the base pattern for the event
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
        public boolean filter(E value) {
            // Use the toMap() method from BaseEvent to get the field values
            Map<String, Object> eventMap = value.toMap();

            // Get the value of the specified variable from the map
            Object fieldValue = eventMap.get(condition.variable());

            // Handle different data types like Float and Boolean
            if (fieldValue instanceof Float) {
                // Print a message when the field is of type Float
                System.out.println("The field value is a Float: " + fieldValue);
                return false; // Return false as we are not applying a condition on Float
            } else if (fieldValue instanceof Boolean variableValue) {
                // Use the boolean value from the condition directly
                boolean conditionValue = condition.value();
                return switch (condition.operator()) {
                    case EQUAL -> variableValue.equals(conditionValue);
                    case NOT_EQUAL -> !variableValue.equals(conditionValue);
                    default -> {
                        System.out.println("Invalid operator for boolean field: " + condition.operator());
                        yield false;
                    }
                };
            } else {
                System.out.println("Variable type is not supported or not found: " + fieldValue);
                return false;
            }
        }
    }
}
