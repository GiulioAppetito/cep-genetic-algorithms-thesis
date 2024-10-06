package cep;

import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.functions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;
import antlr.FlinkCEPParser;  // Importa il parser generato da ANTLR
import antlr.FlinkCEPBaseVisitor;

public class FlinkCEPPatternVisitor extends FlinkCEPBaseVisitor<Pattern<?, ?>> {

    // Metodo per visitare il nodo "pattern" principale
    @Override
    public Pattern<?, ?> visitPattern(FlinkCEPParser.PatternContext ctx) {
        return visitEventSequence(ctx.sequence());  // visita la sequenza di eventi
    }

    // Metodo per visitare una sequenza di eventi
    @Override
    public Pattern<?, ?> visitSequence(FlinkCEPParser.SequenceContext ctx) {
        Pattern<Event, ?> pattern = visitEvent(ctx.condition());  // visita la condizione
        if (ctx.sequence() != null) {
            Pattern<Event, ?> nextPattern = visitSequence(ctx.sequence());  // Ricorsione per visitare la sequenza
            pattern = pattern.next(nextPattern);  // Usa `next` per concatenare gli eventi
        }
        return pattern;
    }

    // Metodo per visitare una condizione
    @Override
    public Pattern<?, ?> visitCondition(FlinkCEPParser.ConditionContext ctx) {
        // Estrai il nome del campo, l'operatore e il valore dalla condizione
        String fieldName = ctx.eventField().getText();
        String comparison = ctx.comparisonOperator().getText();
        String value = ctx.value().getText();

        // Crea un pattern iniziale con il nome del campo
        Pattern<Event, ?> pattern = Pattern.<Event>begin(fieldName)
                .where(new SimpleCondition<Event>() {
                    @Override
                    public boolean filter(Event event) {
                        return evaluateCondition(event, fieldName, comparison, value);
                    }
                });
        return pattern;
    }

    // Metodo per visitare la finestra temporale (timeWindow)
    @Override
    public Pattern<?, ?> visitTimeWindow(FlinkCEPParser.TimeWindowContext ctx) {
        int seconds = Integer.parseInt(ctx.NUMBER().getText());  // Ottieni il valore della finestra in secondi
        Pattern<Event, ?> pattern = visitChildren(ctx);  // Continua con il pattern corrente
        return pattern.within(Time.seconds(seconds));  // Applica la finestra temporale al pattern
    }

    // Funzione per valutare la condizione sugli eventi
    private boolean evaluateCondition(Event event, String fieldName, String comparison, String value) {
        // Esempio semplice: confronta il valore dell'evento con quello specificato nella condizione
        Object fieldValue = event.getField(fieldName);

        switch (comparison) {
            case "==":
                return fieldValue.toString().equals(value);
            case "!=":
                return !fieldValue.toString().equals(value);
            case ">":
                return Double.parseDouble(fieldValue.toString()) > Double.parseDouble(value);
            case "<":
                return Double.parseDouble(fieldValue.toString()) < Double.parseDouble(value);
            default:
                return false;
        }
    }
}
