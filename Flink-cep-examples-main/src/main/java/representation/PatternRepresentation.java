package representation;

import java.io.Serializable;
import cep.events.BaseEvent;
import java.util.List;

public record PatternRepresentation(
        List<SingleEvent> events,
        WithinClause withinClause
) implements Serializable {

    public record SingleEvent(
            Concatenator concatenator,
            String identifier,
            List<Condition> conditions,
            Quantifier quantifier
    ) implements Serializable {
        public enum Concatenator implements Serializable {
            NEXT,
            FOLLOWED_BY,
            FOLLOWED_BY_ANY
        }
    }

    public record WithinClause(
            float duration
    ) implements Serializable {}

    public interface Quantifier extends Serializable {
        enum ParamFree implements Quantifier {
            ONE_OR_MORE,
            OPTIONAL
        }

        record NTimes(int n) implements Quantifier {}
    }

    public record Condition(
            Concatenator concatenator,
            String variable,
            Operator operator,
            float value
    ) implements Serializable {
        public enum Operator implements Serializable {
            EQUAL,
            NOT_EQUAL,
            LESS_THAN,
            GREATER_THAN
        }

        public enum Concatenator implements Serializable {
            AND,
            OR
        }
    }
}
