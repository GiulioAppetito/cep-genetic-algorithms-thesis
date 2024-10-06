grammar FlinkCEP;

// Il pattern è la radice dell'albero che combina eventi e sequenze
pattern
    : eventSequence (timeWindow)?  // Un pattern è una sequenza di eventi con una finestra temporale opzionale
    ;

// La sequenza collega eventi o altre sequenze usando 'next'
eventSequence
    : event (nextSequence)?
    ;

nextSequence
    : 'next' eventSequence  // 'next' collega un evento o una sequenza di eventi
    ;

// Un evento è un nodo foglia dell'albero, con una condizione
event
    : 'begin' condition  // Ogni evento inizia con 'begin' seguito da una condizione
    ;

// Una condizione è la logica associata all'evento (campo, operatore, valore)
condition
    : eventField comparisonOperator value  // Condizione basata su un campo evento, un operatore e un valore
    ;

// La finestra temporale si applica alla sequenza di eventi
timeWindow
    : 'within(' 'Time.seconds(' NUMBER '))'
    ;

// Operatori di confronto
comparisonOperator
    : '==' | '!=' | '>' | '<'
    ;

// Campo dell'evento
eventField
    : ID  // Identificatore del campo evento
    ;

// Valori per il confronto (booleani, stringhe, numeri)
value
    : BOOLEAN | STRING | NUMBER
    ;

// Definizione di tipi di base
BOOLEAN : 'True' | 'False' ;  // Valori booleani
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;  // Identificatori validi per i campi evento
STRING  : '"' (~["\\] | '\\' .)* '"' ;  // Stringhe con caratteri di escape
NUMBER  : [0-9]+ ;  // Numeri interi
WS      : [ \t\r\n]+ -> skip ;  // Ignora spazi bianchi
