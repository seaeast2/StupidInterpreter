package org.sealang.sinterp;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line; // 단순하게 하기 위해 이 토큰이 등장하는 줄수만 저장. 나중에는 컬럼과 길이를 저장해야 함.

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

}
