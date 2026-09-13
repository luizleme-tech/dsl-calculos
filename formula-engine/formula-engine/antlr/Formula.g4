grammar Formula;

assignment
    :  IDENTIFIER ASSIGN expression EOF
    ;

expression
    :  additiveExpression
    ;

additiveExpression
    : multiplicativeExpression
      ((PLUS | MINUS) multiplicativeExpression)*
    ;

multiplicativeExpression
    : powerExpression
      ((MULTIPLY | DIVIDE) powerExpression)*
    ;

powerExpression
    : unaryExpression
      ((POWER) powerExpression)?
    ;

unaryExpression
    : (PLUS | MINUS) unaryExpression
    | primaryExpression
    ;

primaryExpression
    : NUMBER
    | functionCall
    | IDENTIFIER
    | LPAREN expression RPAREN
    ;

functionCall
    : IDENTIFIER LPAREN argumentList? RPAREN
    ;

argumentList
    : expression (COMMA expression)*
    ;

ASSIGN   : '=';
PLUS     : '+';
MINUS    : '-';
MULTIPLY : '*';
DIVIDE   : '/';
POWER    : '^';
LPAREN   : '(';
RPAREN   : ')';
COMMA    : ',';

NUMBER
   : [0-9]+ ('.' [0-9]+)?
   ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;