package app;

import cep.events.BaseEvent;
import org.apache.flink.cep.pattern.Pattern;
import representation.PatternRepresentation;
import representation.mappers.PatternMapper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp.GrowGrammarTreeFactory;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.io.InputStream;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            // Load grammar from resource file
            InputStream grammarStream = Main.class.getResourceAsStream("/equivGrammar.bnf");
            assert grammarStream != null;
            StringGrammar<String> grammar = StringGrammar.load(grammarStream);

            System.out.println("Loaded Grammar: ");
            System.out.println(grammar);

            // Initialize a tree factory with a maximum depth for random generation
            GrowGrammarTreeFactory<String> treeFactory = new GrowGrammarTreeFactory<>(1000, grammar);

            // Generate the random tree
            Tree<String> randomTree = treeFactory.build(new Random(), 10);

            if (randomTree != null) {
                System.out.println("Generated Random Tree:");
                randomTree.prettyPrint(System.out);  // Output the tree structure

                // Apply PatternMapper to convert tree into PatternRepresentation
                System.out.println("\n********** Applying pattern mapper... **********");
                PatternMapper mapper = new PatternMapper();
                PatternRepresentation patternRepresentation = mapper.apply(randomTree); // Map the random tree to PatternRepresentation

                // Output the resulting PatternRepresentation
                System.out.println("\nMapped PatternRepresentation:");
                System.out.println(patternRepresentation);
            } else {
                System.out.println("Random Tree generation returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
