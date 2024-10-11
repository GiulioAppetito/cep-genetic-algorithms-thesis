grammar FlinkCEPGrammar;

// Starting rule for event sequences, beginning with "begin" as in CEP (optional)
pattern
    : ('begin')? (IDENTIFIER)?  eventSequence withinClause?
    ;

// Event sequence definition with binary operators and quantifiers
eventSequence
    : event (binaryOp event)*
    ;

// Event with optional condition and quantifier
event
    : (IDENTIFIER)? (condition)? (quantifier)?
    ;

// Binary operators connecting events to each other
binaryOp
    : 'next'
    | 'followedBy'
    | 'followedByAny'
    ;

// Quantifiers for repeating events
quantifier
    : 'timesOrMore' INT
    | 'times' INT
    | 'oneOrMore'
    | 'optional'
    ;

// Within clause that acts as a parent to duration
withinClause
    : 'within' DURATION
    ;

// Conditions on events (relational operators with variables and values)
condition
    : 'where' conditionExpression
    ;

// Expression to concatenate more conditions
conditionExpression
    : conditionAtom (conditionOp conditionExpression)?
    ;

// Conditions operators
conditionOp
    : 'AND'
    | 'OR'
    | 'NOT'
    ;

// Atomic condition element in an expression
conditionAtom
    : variable relationalOp value
    ;

// Relational operators necessary for conditions
relationalOp
    : '==' | '!=' | '<' | '>' | '<=' | '>='
    ;

// Values used in conditions (boolean, identifier, numeric, and string)
value
    : 'true'
    | 'false'
    | IDENTIFIER
    | INT
    | FLOAT
    | STRING
    ;

// Variables used in conditions
variable
    : IDENTIFIER
    ;

// Duration of the window expressed in seconds, minutes, or hours
DURATION
    : INT 's'   // seconds
    | INT 'm'   // minutes
    | INT 'h'   // hours
    ;

// Lexical rules for integers, floats, strings, and identifiers
INT
    : [0-9]+
    ;

FLOAT
    : [0-9]+ '.' [0-9]+
    ;

STRING
    : '"' (~["\\] | '\\' .)* '"'
    ;

IDENTIFIER
    : [a-zA-Z_0-9][a-zA-Z_0-9]*
    ;

// Skip white and blank spaces
WS
    : [ \t\r\n]+ -> skip
    ;
