package org.sealang.sinterp;

import java.util.ArrayList;
import java.util.List;

import static org.sealang.sinterp.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0; // lexeme의 첫번째 문자
    private int current = 0; // 현재의 위치
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // 우리는 다음 lexeme 앞에 있다.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            case '(' :
                addToken(LPAREN);
                break;

            case ')' :
                addToken(RPAREN);
                break;

            case '{' :
                addToken(LBRACE);
                break;

            case '}' :
                addToken(RBRACE);
                break;

            case ',' :
                addToken(COMMA);
                break;

            case '.' :
                addToken(DOT);
                break;

            case '-' :
                addToken(MINUS);
                break;

            case '+' :
                addToken(PLUS);
                break;

            case ';' :
                addToken(SEMICOLON);
                break;

            case '*' :
                addToken(STAR);
                break;

                // !=, ==, <=, >= 같은 2개짜리 문자 lexeme 처리
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;

            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;

            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;

            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

                // 긴 lexeme 처리
            case '/':
                if (match('/')) {
                    // 라인 끝까지 코멘트
                    while(peek() != '\n' && !isAtEnd())
                        advance();
                }
                else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // 공백은 무시
                break;

            case '\n':
                line++;
                break;

                // 문자열
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                }
                else {
                    SInterp.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // 소수점 이하 찾기
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while(isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            SInterp.error(line, "Unterminated string.");
            return;
        }

        // 닫는 "
        advance();

        // 양 끝단의 " 를 잘라냄
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return'\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() { // lookahead
        current++;
        return source.charAt(current-1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
