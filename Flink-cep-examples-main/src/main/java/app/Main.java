package app;

import org.apache.flink.cep.pattern.Pattern;
import representation.PatternRepresentation;
import representation.mappers.RepresentationToPatternMapper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.mappers.TreeToRepresentationMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Caricamento della grammatica dal file di risorse
            InputStream grammarStream = Main.class.getResourceAsStream("/FlinkCEPGrammar.bnf");
            assert grammarStream != null;
            StringGrammar<String> grammar = StringGrammar.load(grammarStream);

            // Parametri per la generazione dell'albero
            int MAX_HEIGHT = 100;
            int TARGET_DEPTH = 10;

            System.out.println("Loaded Grammar: ");
            System.out.println(grammar);

            // Inizializza un factory per l'albero con profondità massima per la generazione casuale
            GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(MAX_HEIGHT, grammar);

            // Genera l'albero casuale
            Tree<String> randomTree = treeFactory.build(new Random(), TARGET_DEPTH);

            if (randomTree != null) {
                System.out.println("Generated Random Tree:");
                randomTree.prettyPrint(System.out);  // Stampa la struttura dell'albero

                // Applica TreeToRepresentationMapper per convertire l'albero in PatternRepresentation
                System.out.println("\n********** Applying pattern mapper... **********");
                TreeToRepresentationMapper toRepresentationMapper = new TreeToRepresentationMapper();
                PatternRepresentation patternRepresentation = toRepresentationMapper.apply(randomTree);

                // Stampa il PatternRepresentation risultante
                System.out.println("\nMapped PatternRepresentation:\n");
                System.out.println(patternRepresentation);

                // Converte PatternRepresentation in un Pattern di Flink
                System.out.println("\n********** Converting to Flink Pattern... **********");
                RepresentationToPatternMapper toPatternMapper = new RepresentationToPatternMapper();
                Pattern<Map<String, Float>, ?> flinkPattern = toPatternMapper.convert(patternRepresentation);

                // Stampa il Pattern di Flink
                System.out.println("\nGenerated Flink Pattern:\n");
                System.out.println(flinkPattern);
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
