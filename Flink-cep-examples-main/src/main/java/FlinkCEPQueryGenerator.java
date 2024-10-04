import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class FlinkCEPQueryGenerator {

    public static void main(String[] args) throws Exception {
        // Esempio di input che rappresenta la query Flink CEP
        String inputPattern = "begin ip_address == \"129.16.0.32\" next successful_login == True within(Time.seconds(10))";

        // Crea il lexer e il parser generati da ANTLR
        CharStream input = CharStreams.fromString(inputPattern);
        FlinkCEPLexer lexer = new FlinkCEPLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlinkCEPParser parser = new FlinkCEPParser(tokens);

        // Parsea l'input e ottieni l'albero sintattico (AST)
        ParseTree tree = parser.pattern();
        System.out.println("Albero Sintattico: " + tree.toStringTree(parser)); // Stampa l'albero sintattico

        // Usa un visitor per generare il pattern Flink CEP
        FlinkCEPGenerator generator = new FlinkCEPGenerator();
        Pattern<Event, ?> flinkPattern = generator.visit(tree);

        // Stampa il pattern Flink CEP generato
        System.out.println("Pattern Flink CEP generato: " + flinkPattern);
    }
}
