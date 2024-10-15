package antlr;// Generated from C:/Users/giuli/IdeaProjects/Sandbox_Thesis_Chalmers_2/Flink-cep-examples-main/src/main/resources/FlinkCEPGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FlinkCEPGrammarParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FlinkCEPGrammarVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(FlinkCEPGrammarParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#eventSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventSequence(FlinkCEPGrammarParser.EventSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent(FlinkCEPGrammarParser.EventContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#binaryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOp(FlinkCEPGrammarParser.BinaryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#quantifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuantifier(FlinkCEPGrammarParser.QuantifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#withinClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithinClause(FlinkCEPGrammarParser.WithinClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(FlinkCEPGrammarParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#conditionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionExpression(FlinkCEPGrammarParser.ConditionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#conditionOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionOp(FlinkCEPGrammarParser.ConditionOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#conditionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionAtom(FlinkCEPGrammarParser.ConditionAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#relationalOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalOp(FlinkCEPGrammarParser.RelationalOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(FlinkCEPGrammarParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FlinkCEPGrammarParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(FlinkCEPGrammarParser.VariableContext ctx);
}