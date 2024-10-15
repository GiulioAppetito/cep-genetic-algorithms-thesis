package antlr;// Generated from C:/Users/giuli/IdeaProjects/Sandbox_Thesis_Chalmers_2/Flink-cep-examples-main/src/main/resources/FlinkCEPGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FlinkCEPGrammarParser}.
 */
public interface FlinkCEPGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(FlinkCEPGrammarParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(FlinkCEPGrammarParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#eventSequence}.
	 * @param ctx the parse tree
	 */
	void enterEventSequence(FlinkCEPGrammarParser.EventSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#eventSequence}.
	 * @param ctx the parse tree
	 */
	void exitEventSequence(FlinkCEPGrammarParser.EventSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#event}.
	 * @param ctx the parse tree
	 */
	void enterEvent(FlinkCEPGrammarParser.EventContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#event}.
	 * @param ctx the parse tree
	 */
	void exitEvent(FlinkCEPGrammarParser.EventContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#binaryOp}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOp(FlinkCEPGrammarParser.BinaryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#binaryOp}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOp(FlinkCEPGrammarParser.BinaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void enterQuantifier(FlinkCEPGrammarParser.QuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#quantifier}.
	 * @param ctx the parse tree
	 */
	void exitQuantifier(FlinkCEPGrammarParser.QuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#withinClause}.
	 * @param ctx the parse tree
	 */
	void enterWithinClause(FlinkCEPGrammarParser.WithinClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#withinClause}.
	 * @param ctx the parse tree
	 */
	void exitWithinClause(FlinkCEPGrammarParser.WithinClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(FlinkCEPGrammarParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(FlinkCEPGrammarParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#conditionExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionExpression(FlinkCEPGrammarParser.ConditionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#conditionExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionExpression(FlinkCEPGrammarParser.ConditionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#conditionOp}.
	 * @param ctx the parse tree
	 */
	void enterConditionOp(FlinkCEPGrammarParser.ConditionOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#conditionOp}.
	 * @param ctx the parse tree
	 */
	void exitConditionOp(FlinkCEPGrammarParser.ConditionOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#conditionAtom}.
	 * @param ctx the parse tree
	 */
	void enterConditionAtom(FlinkCEPGrammarParser.ConditionAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#conditionAtom}.
	 * @param ctx the parse tree
	 */
	void exitConditionAtom(FlinkCEPGrammarParser.ConditionAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#relationalOp}.
	 * @param ctx the parse tree
	 */
	void enterRelationalOp(FlinkCEPGrammarParser.RelationalOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#relationalOp}.
	 * @param ctx the parse tree
	 */
	void exitRelationalOp(FlinkCEPGrammarParser.RelationalOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(FlinkCEPGrammarParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(FlinkCEPGrammarParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPGrammarParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(FlinkCEPGrammarParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPGrammarParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(FlinkCEPGrammarParser.VariableContext ctx);
}