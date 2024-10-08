package org.sealang.sinterp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SInterp {
    private static final Interpreter interpreter = new Interpreter();

    // true 이면 코드를 더이상 실행하지 않도록 한다.
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: sintp [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // 파일로 실행할 때
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // 종료할 때 에러 코드를 명시
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    // 커맨드라인으로 실행할 때. Ctrl+D 로 종료 가능
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false; // 커맨드라인 모드에서는 한줄마다 에러 리셋
        }
    }

    private static void run(String source) {
        // Scanner 를 통해 한줄에 대한 토큰 뭉치를 생성
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // 앞서 만든 토큰을 파싱후 AST 구축
        Parser parser = new Parser(tokens);
        //Expr expression = parser.parse();
        List<Stmt> statements = parser.parse();

        // 구문 에러 발생 시 멈춘다.
        if (hadError)
            return;

        // 변수 리졸브
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // 레졸루션 에러 발생 시 멈춘다.
        if (hadError)
            return;

        // 인터프리터에서 AST 를 실행
        interpreter.interpret(statements);
    }
    
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    private static void report(int line, String where, String message) {
        System.out.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
