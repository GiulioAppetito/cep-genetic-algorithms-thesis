grammar FlinkCEPGrammar;

// Starting rule for event sequences
sequence
    : binaryOpEvent ('within' DURATION)? #EventSequenceWithDuration
    ;

// Event with binary operator (ensuring operators are internal nodes)
binaryOpEvent
    : event binaryOp event  #BinaryOpNode
    | event                #SingleEvent
    ;

// Definition of a single event with an optional condition
event
    : IDENTIFIER (condition)?  #EventWithCondition
    ;

// Binary operators are now internal nodes with sub-events
binaryOp
    : 'next'         #NextOperator
    | 'followedBy'   #FollowedByOperator
    | 'followedByAny' #FollowedByAnyOperator
    ;

// Conditions on events (relational operators with variables and values)
condition
    : 'where' variable relationalOp value  #ConditionExpr
    ;

// Relational operators for conditions
relationalOp
    : '==' | '!=' | '<' | '>' | '<=' | '>='
    ;

// Values used in conditions (boolean, identifier, numeric, or string)
value
    : 'true'
    | 'false'
    | IDENTIFIER
    | INT
    | FLOAT
    | STRING
    ;

// Variables for conditions
variable
    : IDENTIFIER
    ;

// Duration expressed in seconds, minutes, or hours
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
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

// Ignore whitespace
WS
    : [ \t\r\n]+ -> skip
    ;
