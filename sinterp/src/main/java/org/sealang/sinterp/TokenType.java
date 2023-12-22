package org.sealang.sinterp;

public enum TokenType {
    // Single-Character tokens.
    LPAREN,     // (
    RPAREN,     // )
    LBRACE,     // {
    RBRACE,     // }
    COMMA,      // ,
    DOT,        // .
    MINUS,      // -
    PLUS,       // +
    SEMICOLON,  // ;
    SLASH,      // /
    STAR,       // *

    // One or Two Character Tokens.
    BANG,       // !
    BANG_EQUAL, // !=
    EQUAL,      // =
    EQUAL_EQUAL, // ==
    GREATER,    // >
    GREATER_EQUAL, // >=
    LESS,       // <
    LESS_EQUAL, // <=

    // Lieterals
    IDENTIFIER, // 식별자
    STRING,     // 문자열
    NUMBER,     // 숫자

    // Keyword
    AND,        // and
    CLASS,      // class
    ELSE,       // else
    FALSE,      // false
    FUN,        // fun
    FOR,        // for
    IF,         // if
    NIL,        // nil
    OR,         // or
    PRINT,      // print
    RETURN,     // return
    SUPER,      // super
    THIS,       // this
    TRUE,       // true
    VAR,        // var
    WHILE,      // while

    EOF

}
