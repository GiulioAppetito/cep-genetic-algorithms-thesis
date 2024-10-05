package cep;

import antlr.FlinkCEPBaseVisitor;
import antlr.FlinkCEPParser;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class FlinkCEPVisitor extends FlinkCEPBaseVisitor<Pattern<Event, ?>> {

    private Pattern<Event, ?> pattern;

    @Override
    public Pattern<Event, ?> visitPattern(FlinkCEPParser.PatternContext ctx) {
        // Inizia con il primo evento (begin)
        if (ctx.begin() != null) {
            // Usa la condizione generata dal visitCondition
            SimpleCondition<Event> condition = getCondition(ctx.condition());
            pattern = Pattern.<Event>begin("start").where(condition);
        }

        // Aggiunge la sequenza (next) se presente
        if (ctx.sequence() != null) {
            visit(ctx.sequence());
        }

        // Aggiunge il vincolo temporale (within) se presente
        if (ctx.timeWindow() != null) {
            long seconds = Long.parseLong(ctx.timeWindow().NUMBER().getText());
            pattern = pattern.within(Time.seconds(seconds));
        }

        return pattern;
    }

    @Override
    public Pattern<Event, ?> visitSequence(FlinkCEPParser.SequenceContext ctx) {
        // Genera la condizione per la sequenza
        SimpleCondition<Event> condition = getCondition(ctx.condition());
        pattern = pattern.next("next").where(condition);

        // Se c'è una sequenza successiva, la visita
        if (ctx.sequence() != null) {
            visit(ctx.sequence());
        }

        return pattern;
    }

    /**
     * Questo metodo restituisce una SimpleCondition<Event>, ma non implementa direttamente un visitCondition.
     * Questo ti consente di creare condizioni da usare nei pattern.
     */
    private SimpleCondition<Event> getCondition(FlinkCEPParser.ConditionContext ctx) {
        String field = ctx.eventField().getText();
        String operator = ctx.comparisonOperator().getText();
        String value = ctx.value().getText();

        // Restituisce una SimpleCondition per Flink CEP
        return new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event event) {
                Object fieldValue = event.getField(field);
                switch (operator) {
                    case "==":
                        return fieldValue.toString().equals(value);
                    case "!=":
                        return !fieldValue.toString().equals(value);
                    case ">":
                        return ((Comparable) fieldValue).compareTo(value) > 0;
                    case "<":
                        return ((Comparable) fieldValue).compareTo(value) < 0;
                    default:
                        throw new IllegalArgumentException("Operatore sconosciuto: " + operator);
                }
            }
        };
    }
}
