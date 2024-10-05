// Generated from C:/Users/giuli/IdeaProjects/Sandbox_Thesis_Chalmers_2/Flink-cep-examples-main/src/main/antlr4/FlinkCEP.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class FlinkCEPLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		BOOLEAN=10, ID=11, STRING=12, NUMBER=13, WS=14;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"BOOLEAN", "ID", "STRING", "NUMBER", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'begin'", "'next'", "'=='", "'!='", "'>'", "'<'", "'within('", 
			"'Time.seconds('", "'))'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "BOOLEAN", 
			"ID", "STRING", "NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public FlinkCEPLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FlinkCEP.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u000et\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b"+
		"\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\tU\b\t\u0001\n\u0001\n\u0005\nY\b\n\n\n\f\n\\\t\n\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000bb\b\u000b\n\u000b"+
		"\f\u000be\t\u000b\u0001\u000b\u0001\u000b\u0001\f\u0004\fj\b\f\u000b\f"+
		"\f\fk\u0001\r\u0004\ro\b\r\u000b\r\f\rp\u0001\r\u0001\r\u0000\u0000\u000e"+
		"\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r"+
		"\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e"+
		"\u0001\u0000\u0005\u0003\u0000AZ__az\u0004\u000009AZ__az\u0002\u0000\""+
		"\"\\\\\u0001\u000009\u0003\u0000\t\n\r\r  y\u0000\u0001\u0001\u0000\u0000"+
		"\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000"+
		"\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000"+
		"\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000"+
		"\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000"+
		"\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000"+
		"\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000"+
		"\u001b\u0001\u0000\u0000\u0000\u0001\u001d\u0001\u0000\u0000\u0000\u0003"+
		"#\u0001\u0000\u0000\u0000\u0005(\u0001\u0000\u0000\u0000\u0007+\u0001"+
		"\u0000\u0000\u0000\t.\u0001\u0000\u0000\u0000\u000b0\u0001\u0000\u0000"+
		"\u0000\r2\u0001\u0000\u0000\u0000\u000f:\u0001\u0000\u0000\u0000\u0011"+
		"H\u0001\u0000\u0000\u0000\u0013T\u0001\u0000\u0000\u0000\u0015V\u0001"+
		"\u0000\u0000\u0000\u0017]\u0001\u0000\u0000\u0000\u0019i\u0001\u0000\u0000"+
		"\u0000\u001bn\u0001\u0000\u0000\u0000\u001d\u001e\u0005b\u0000\u0000\u001e"+
		"\u001f\u0005e\u0000\u0000\u001f \u0005g\u0000\u0000 !\u0005i\u0000\u0000"+
		"!\"\u0005n\u0000\u0000\"\u0002\u0001\u0000\u0000\u0000#$\u0005n\u0000"+
		"\u0000$%\u0005e\u0000\u0000%&\u0005x\u0000\u0000&\'\u0005t\u0000\u0000"+
		"\'\u0004\u0001\u0000\u0000\u0000()\u0005=\u0000\u0000)*\u0005=\u0000\u0000"+
		"*\u0006\u0001\u0000\u0000\u0000+,\u0005!\u0000\u0000,-\u0005=\u0000\u0000"+
		"-\b\u0001\u0000\u0000\u0000./\u0005>\u0000\u0000/\n\u0001\u0000\u0000"+
		"\u000001\u0005<\u0000\u00001\f\u0001\u0000\u0000\u000023\u0005w\u0000"+
		"\u000034\u0005i\u0000\u000045\u0005t\u0000\u000056\u0005h\u0000\u0000"+
		"67\u0005i\u0000\u000078\u0005n\u0000\u000089\u0005(\u0000\u00009\u000e"+
		"\u0001\u0000\u0000\u0000:;\u0005T\u0000\u0000;<\u0005i\u0000\u0000<=\u0005"+
		"m\u0000\u0000=>\u0005e\u0000\u0000>?\u0005.\u0000\u0000?@\u0005s\u0000"+
		"\u0000@A\u0005e\u0000\u0000AB\u0005c\u0000\u0000BC\u0005o\u0000\u0000"+
		"CD\u0005n\u0000\u0000DE\u0005d\u0000\u0000EF\u0005s\u0000\u0000FG\u0005"+
		"(\u0000\u0000G\u0010\u0001\u0000\u0000\u0000HI\u0005)\u0000\u0000IJ\u0005"+
		")\u0000\u0000J\u0012\u0001\u0000\u0000\u0000KL\u0005T\u0000\u0000LM\u0005"+
		"r\u0000\u0000MN\u0005u\u0000\u0000NU\u0005e\u0000\u0000OP\u0005F\u0000"+
		"\u0000PQ\u0005a\u0000\u0000QR\u0005l\u0000\u0000RS\u0005s\u0000\u0000"+
		"SU\u0005e\u0000\u0000TK\u0001\u0000\u0000\u0000TO\u0001\u0000\u0000\u0000"+
		"U\u0014\u0001\u0000\u0000\u0000VZ\u0007\u0000\u0000\u0000WY\u0007\u0001"+
		"\u0000\u0000XW\u0001\u0000\u0000\u0000Y\\\u0001\u0000\u0000\u0000ZX\u0001"+
		"\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\u0016\u0001\u0000\u0000"+
		"\u0000\\Z\u0001\u0000\u0000\u0000]c\u0005\"\u0000\u0000^b\b\u0002\u0000"+
		"\u0000_`\u0005\\\u0000\u0000`b\t\u0000\u0000\u0000a^\u0001\u0000\u0000"+
		"\u0000a_\u0001\u0000\u0000\u0000be\u0001\u0000\u0000\u0000ca\u0001\u0000"+
		"\u0000\u0000cd\u0001\u0000\u0000\u0000df\u0001\u0000\u0000\u0000ec\u0001"+
		"\u0000\u0000\u0000fg\u0005\"\u0000\u0000g\u0018\u0001\u0000\u0000\u0000"+
		"hj\u0007\u0003\u0000\u0000ih\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000"+
		"\u0000ki\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000l\u001a\u0001"+
		"\u0000\u0000\u0000mo\u0007\u0004\u0000\u0000nm\u0001\u0000\u0000\u0000"+
		"op\u0001\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000"+
		"\u0000qr\u0001\u0000\u0000\u0000rs\u0006\r\u0000\u0000s\u001c\u0001\u0000"+
		"\u0000\u0000\u0007\u0000TZackp\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}