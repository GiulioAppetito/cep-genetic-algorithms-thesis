grammar FlinkCEP;

pattern
    : begin condition sequence? timeWindow?
    ;

begin
    : 'begin' | 'next'
    ;

condition
    : eventField comparisonOperator value
    ;

eventField
    : ID
    ;

comparisonOperator
    : '==' | '!=' | '>' | '<'
    ;

value
    : BOOLEAN | STRING | NUMBER
    ;

sequence
    : 'next' condition sequence?  // Consenti che "next" sia seguito direttamente da una condizione
    ;

timeWindow
    : 'within(' 'Time.seconds(' NUMBER '))'
    ;

BOOLEAN : 'True' | 'False' ;  // Metti BOOLEAN sopra ID per priorità
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING  : '"' (~["\\] | '\\' .)* '"' ;
NUMBER  : [0-9]+ ;
WS      : [ \t\r\n]+ -> skip ;
