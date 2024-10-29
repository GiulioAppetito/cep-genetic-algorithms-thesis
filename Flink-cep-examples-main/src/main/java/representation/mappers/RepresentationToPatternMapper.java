package representation.mappers;

import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import representation.PatternRepresentation;

import java.time.Duration;
import java.util.List;

public class RepresentationToPatternMapper<E> {

    public Pattern<E, ?> convert(PatternRepresentation representation) {
        List<PatternRepresentation.Event> events = representation.events();
        Pattern<E, E> flinkPattern = null;

        for (PatternRepresentation.Event event : events) {
            Pattern<E, E> newPattern = createPatternForEvent(event);

            if (flinkPattern == null) {
                flinkPattern = newPattern;
            } else {
                switch (event.concatenator()) {
                    case NEXT -> flinkPattern = flinkPattern.next(newPattern);
                    case FOLLOWED_BY -> flinkPattern = flinkPattern.followedBy(newPattern);
                    case FOLLOWED_BY_ANY -> flinkPattern = flinkPattern.followedByAny(newPattern);
                }
            }
        }

        if (representation.withinClause() != null) {
            flinkPattern = flinkPattern.within(Duration.ofSeconds((long) representation.withinClause().duration()));
        }

        return flinkPattern;
    }

    private Pattern<E, E> createPatternForEvent(PatternRepresentation.Event event) {
        Pattern<E, E> pattern = Pattern.<E>begin(event.identifier());

        if (event.quantifier() instanceof PatternRepresentation.Quantifier.ParamFree quantifier) {
            switch (quantifier) {
                case ONE_OR_MORE -> pattern = pattern.oneOrMore();
                case OPTIONAL -> pattern = pattern.optional();
            }
        } else if (event.quantifier() instanceof PatternRepresentation.Quantifier.NTimes nTimes) {
            pattern = pattern.times(nTimes.n());
        }

        for (PatternRepresentation.Condition condition : event.conditions()) {
            pattern = pattern.where(new EventCondition<>(condition));
        }

        return pattern;
    }

    private static class EventCondition<E> extends IterativeCondition<E> {
        private final PatternRepresentation.Condition condition;

        public EventCondition(PatternRepresentation.Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean filter(E value, Context<E> ctx) throws Exception {
            if (value instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Float> map = (java.util.Map<String, Float>) value;
                Float variableValue = map.get(condition.variable());
                System.out.println("Filtering event: " + value + " with condition: " + condition);

                if (variableValue == null) {
                    System.out.println("Variable value is null for " + condition.variable());
                    return false;
                }

                return switch (condition.operator()) {
                    case EQUAL -> variableValue.equals(condition.value());
                    case NOT_EQUAL -> !variableValue.equals(condition.value());
                    case LESS_THAN -> variableValue < condition.value();
                    case GREATER_THAN -> variableValue > condition.value();
                };
            }
            return false;
        }
    }
}
