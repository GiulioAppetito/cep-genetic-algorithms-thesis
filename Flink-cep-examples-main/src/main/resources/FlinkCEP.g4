grammar FlinkCEP;

pattern
    : eventSequence (timeWindow)?  // Pattern principale con una sequenza di eventi e finestra temporale opzionale
    ;

eventSequence
    : event (nextEvent | followedByEvent | followedByAnyEvent | notNextEvent | notFollowedByEvent)?
    ;  // Supporto per next, followedBy, notNext, ecc.

nextEvent
    : 'next' eventSequence
    ;

followedByEvent
    : 'followedBy' eventSequence  // Contiguità rilassata
    ;

followedByAnyEvent
    : 'followedByAny' eventSequence  // Contiguità non deterministica
    ;

notNextEvent
    : 'notNext' eventSequence  // Negazione con contiguità stretta
    ;

notFollowedByEvent
    : 'notFollowedBy' eventSequence  // Negazione con contiguità rilassata
    ;

event
    : 'begin' conditions quantifier?
    ;

conditions
    : condition (logicalOperator condition)*
    ;

condition
    : eventField comparisonOperator value
    ;

quantifier
    : 'times(' NUMBER ')'
    | 'oneOrMore()'  // Aggiungi supporto per 'oneOrMore'
    | 'optional()'   // Aggiungi supporto per 'optional'
    | 'timesOrMore(' NUMBER ')'  // Aggiungi supporto per 'timesOrMore'
    ;

logicalOperator
    : 'AND' | 'OR'
    ;

timeWindow
    : 'within(' 'Time.seconds(' NUMBER '))'
    ;

comparisonOperator
    : '==' | '!=' | '>' | '<'
    ;

eventField
    : ID
    ;

value
    : BOOLEAN | STRING | NUMBER
    ;

BOOLEAN : 'True' | 'False' ;
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING  : '"' (~["\\] | '\\' .)* '"' ;
NUMBER  : [0-9]+ ;
WS      : [ \t\r\n]+ -> skip ;
