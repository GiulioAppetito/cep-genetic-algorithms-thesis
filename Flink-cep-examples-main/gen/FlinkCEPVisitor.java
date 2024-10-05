// Generated from C:/Users/giuli/IdeaProjects/Sandbox_Thesis_Chalmers_2/Flink-cep-examples-main/src/main/antlr4/FlinkCEP.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FlinkCEPParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FlinkCEPVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(FlinkCEPParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#begin}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBegin(FlinkCEPParser.BeginContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(FlinkCEPParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#eventField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventField(FlinkCEPParser.EventFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(FlinkCEPParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(FlinkCEPParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence(FlinkCEPParser.SequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPParser#timeWindow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeWindow(FlinkCEPParser.TimeWindowContext ctx);
}