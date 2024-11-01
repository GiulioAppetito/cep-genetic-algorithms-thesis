package representation;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public record PatternRepresentation(
        List<Event> events,
        WithinClause withinClause
) implements Serializable {

    @Override
    public String toString() {
        String eventsStr = events.stream()
                .map(Event::toString)
                .collect(Collectors.joining(",\n  "));

        return "PatternRepresentation {\n" +
                "  events=[\n  " + eventsStr + "\n  ],\n" +
                "  withinClause=" + withinClause + "\n" +
                "}";
    }

    public record Event(
            String identifier,
            List<Condition> conditions,
            Quantifier quantifier,
            Concatenator concatenator
    ) implements Serializable {
        @Override
        public String toString() {
            String conditionsStr = conditions.stream()
                    .map(Condition::toString)
                    .collect(Collectors.joining(", "));

            return "Event {\n" +
                    "    identifier='" + identifier + "',\n" +
                    "    conditions=[" + conditionsStr + "],\n" +
                    "    quantifier=" + quantifier + ",\n" +
                    "    concatenator=" + concatenator + "\n" +
                    "  }";
        }

        // Defines the possible concatenators between events
        public enum Concatenator implements Serializable {
            NEXT, FOLLOWED_BY, FOLLOWED_BY_ANY
        }
    }

    // Represents a within clause that defines the time constraint for a sequence
    public record WithinClause(float duration) implements Serializable {
        @Override
        public String toString() {
            return "WithinClause { duration=" + duration + " }";
        }
    }

    // Represents a quantifier for how many times an event should occur
    public interface Quantifier extends Serializable {
        enum ParamFree implements Quantifier {
            ONE_OR_MORE, OPTIONAL
        }

        // Quantifier for specifying an exact number of occurrences
        record NTimes(int n) implements Quantifier {
            @Override
            public String toString() {
                return "NTimes { n=" + n + " }";
            }
        }
    }

    // Represents a condition associated with an event
    public record Condition(
            String variable,
            Operator operator,
            boolean value,  // Updated to boolean type
            Concatenator concatenator
    ) implements Serializable {
        @Override
        public String toString() {
            return "Condition { variable='" + variable + "', operator=" + operator + ", value=" + value +
                    (concatenator != null ? ", concatenator=" + concatenator : "") + " }";
        }

        // Defines possible operators for conditions
        public enum Operator implements Serializable {
            EQUAL, NOT_EQUAL
        }

        // Defines logical concatenators for conditions (AND, OR)
        public enum Concatenator implements Serializable {
            AND, OR
        }
    }
}
