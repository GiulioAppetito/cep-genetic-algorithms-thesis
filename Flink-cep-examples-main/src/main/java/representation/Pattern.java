package representation;

import cep.events.BaseEvent;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.List;

public record Pattern(
        List<Event> events,
        WithinClause withinClause
) {

    public record Event(
            Concatenator concatenator,
            String identifier,
            List<Condition> conditions,
            Quantifier quantifier
    ){
        public enum Concatenator{
            NEXT,
            FOLLOWED_BY,
            FOLLOWED_BY_ANY
        }
    }
    public record WithinClause(
            float duration
    ){

    }
    public interface Quantifier{
        enum ParamFree implements Quantifier{
            ONE_OR_MORE,
            OPTIONAL
        }
        record NTimes(int n) implements Quantifier{

        }
    }

    public record Condition(
            Concatenator concatenator,
            String variable,
            Operator operator,
            float value
    ){
        public enum Operator{
            EQUAL,
            NOT_EQUAL,
            LESS_THAN,
            GREATER_THAN
        }
        public enum Concatenator{
            AND,
            OR
        }
    }
    public org.apache.flink.cep.pattern.Pattern<BaseEvent, ?> toCEPPattern() {
        for(Event event : events) {
            System.out.println(event);
        }
        return null;
    }

    public static void main(String[] args) {
        Pattern p = new Pattern(
                List.of(
                        new Event(
                                Event.Concatenator.NEXT,
                                "pluto",
                                List.of(),
                                Quantifier.ParamFree.ONE_OR_MORE
                        )
                ),
                new WithinClause(1)
        );
        System.out.println(p);
    }

}
