package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Run on a file
     * @param path Path to the source file
     * @throws IOException
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code
        if (hadError) {
            System.exit(65);
        }
    }

    /**
     * Run in CLI mode
     * @throws IOException
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    /**
     * Run Interpreter
     * @param source The source text
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Reports a generic error
     *
     * @param line    Line number
     * @param message Error message
     */
    static void error(int line, String message) {
        reportError(line, "", message);
    }

    /**
     * Reports an error regarding a token
     *
     * @param token   The erroring token
     * @param message Error message
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            reportError(token.line, " at end", message);
        } else {
            reportError(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    /**
     * Prints an error to the console
     *
     * @param line    Line number
     * @param where   Position in line
     * @param message Error message
     */
    private static void reportError(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
