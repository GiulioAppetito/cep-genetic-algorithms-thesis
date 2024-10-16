package cep.utils;

import antlr.FlinkCEPGrammarLexer;
import antlr.FlinkCEPGrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.flink.cep.pattern.Pattern;
import cep.events.BaseEvent;

public class FlinkCEPPatternGenerator {

    public static <T extends BaseEvent> Pattern<T, ?> generatePattern(String patternString) {
        System.out.println("Parsing pattern string: " + patternString);

        // Lexer and parser generation
        FlinkCEPGrammarLexer lexer = new FlinkCEPGrammarLexer(CharStreams.fromString(patternString));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlinkCEPGrammarParser parser = new FlinkCEPGrammarParser(tokens);

        // Obtain the context
        FlinkCEPGrammarParser.PatternContext context = parser.pattern();

        // From context to Flink CEP Pattern through the visitor
        FlinkCEPPatternVisitor<T> visitor = new FlinkCEPPatternVisitor<>();
        Pattern<T, ?> pattern = (Pattern<T, ?>) visitor.visit(context);

        System.out.println("Pattern generato con successo: " + pattern + "\n\n*******************************************************************\n");
        return pattern;
    }
}
