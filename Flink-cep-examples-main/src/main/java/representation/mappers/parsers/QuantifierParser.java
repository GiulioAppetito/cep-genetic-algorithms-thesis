package representation.mappers.parsers;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import representation.PatternRepresentation;

public class QuantifierParser {

    public static PatternRepresentation.Quantifier parseQuantifier(Tree<String> quantifierNode) {
        Tree<String> quantNode = quantifierNode.child(0);
        return switch (quantNode.content()) {
            case "oneOrMore" -> PatternRepresentation.Quantifier.ParamFree.ONE_OR_MORE;
            case "optional" -> PatternRepresentation.Quantifier.ParamFree.OPTIONAL;
            case "<greaterThanZeroNum>" -> new PatternRepresentation.Quantifier.NTimes(
                    Integer.parseInt(quantNode.visitLeaves().get(0))
            );
            default -> throw new IllegalArgumentException("Unknown quantifier: " + quantNode.content());
        };
    }
}
