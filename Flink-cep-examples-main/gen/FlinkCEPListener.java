// Generated from C:/Users/giuli/IdeaProjects/Sandbox_Thesis_Chalmers_2/Flink-cep-examples-main/src/main/antlr4/FlinkCEP.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FlinkCEPParser}.
 */
public interface FlinkCEPListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(FlinkCEPParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(FlinkCEPParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#begin}.
	 * @param ctx the parse tree
	 */
	void enterBegin(FlinkCEPParser.BeginContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#begin}.
	 * @param ctx the parse tree
	 */
	void exitBegin(FlinkCEPParser.BeginContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(FlinkCEPParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(FlinkCEPParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#eventField}.
	 * @param ctx the parse tree
	 */
	void enterEventField(FlinkCEPParser.EventFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#eventField}.
	 * @param ctx the parse tree
	 */
	void exitEventField(FlinkCEPParser.EventFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(FlinkCEPParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(FlinkCEPParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(FlinkCEPParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(FlinkCEPParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#sequence}.
	 * @param ctx the parse tree
	 */
	void enterSequence(FlinkCEPParser.SequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#sequence}.
	 * @param ctx the parse tree
	 */
	void exitSequence(FlinkCEPParser.SequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlinkCEPParser#timeWindow}.
	 * @param ctx the parse tree
	 */
	void enterTimeWindow(FlinkCEPParser.TimeWindowContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlinkCEPParser#timeWindow}.
	 * @param ctx the parse tree
	 */
	void exitTimeWindow(FlinkCEPParser.TimeWindowContext ctx);
}