package org.sealang.sinterp;

import java.util.ArrayList;
import java.util.List;

import static org.sealang.sinterp.TokenType.*;

/*
전체 문법 :
    program     → statement* EOF ;

    declaration → varDecl
                | statement;

    statement   → exprStmt
                | printStmt ;

    varDecl     → "var" IDENTIFIER ( "=" expression )? ";";

    exprStmt    → expression ";" ;
    printStmt   → "print" expression ";" ;

    expression  → assignment ;
    assignment  → IDENTIFIER "=" assignment
                | equality ;

    equality    → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term        → factor ( ( "-" | "+" ) factor )* ;
    factor      → unary ( ( "/" | "*" ) unary )* ;
    unary       → ( "!" | "-" ) unary | primary ;

    primary     → "true" | "false" | "nil"
                | NUMBER | STRING
                | "(" expression ")"
                | IDENTIFIER ;
 */


// AST 구축
class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /*Expr parse() {
        try {
            return expression();
        }
        catch (ParseError error) {
            return null;
        }
    }*/
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            //statements.add(statement());
            statements.add(declaration());
        }

        return statements;
    }


    // expression  → assignment ;
    private Expr expression() {
        return assignment();
    }

    // declaration() 은 에러 복구를 하기에 적당한 위치이다.
    private Stmt declaration() {
        try { // 에러를 복구 하기 위해 try-catch 문으로 감싼다.
            if(match(VAR))
                return varDeclaration();
            return statement();
        }
        catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(PRINT))
            return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /*
    assignment  → IDENTIFIER "=" assignment
                | equality ;
     */
    private Expr assignment() {
        Expr expr = equality(); // identifier 혹은 equality 임

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            // expr 이 변수(Variable)인지 확인하여, 변수면 Assign 노드 구성
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //term → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary → ( "!" | "-" ) unary | primary ;
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LPAREN)) {
            Expr expr = expression();
            consume(RPAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) { // token 을 소비하지 않는다.
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message) {
        SInterp.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
