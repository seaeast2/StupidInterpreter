package org.sealang.sinterp;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SInterp {

    private static boolean hadError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
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
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // 지금은 단지 토큰을 출력한다.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
    
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    private static void report(int line, String where, String message) {
        System.out.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
}
