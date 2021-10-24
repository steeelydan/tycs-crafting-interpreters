package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private static final Map<String, TokenType> keywords;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Consume all chars in source and tokenize them
     *
     * @return Token list
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    /**
     * Detect and tokenize a single token; add it to token list
     */
    private void scanToken() {
        char character = advance();

        switch (character) {
        case '(':
            addToken(LEFT_PAREN);
            break;
        case ')':
            addToken(RIGHT_PAREN);
            break;
        case '{':
            addToken(LEFT_BRACE);
            break;
        case '}':
            addToken(RIGHT_BRACE);
            break;
        case ',':
            addToken(COMMA);
            break;
        case '.':
            addToken(DOT);
            break;
        case '-':
            addToken(MINUS);
            break;
        case '+':
            addToken(PLUS);
            break;
        case ';':
            addToken(SEMICOLON);
            break;
        case '*':
            addToken(STAR);
            break;
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
        case '/':
            if (match('/')) {
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
            } else {
                addToken(SLASH);
            }
            break;

        case ' ':
        case '\r':
        case '\t':
            // Ignore whitespace.
            break;

        case '\n':
            line++;
            break;

        case '"':
            string();
            break;

        default:
            if (isDigit(character)) {
                number();
            } else if (isAlpha(character)) {
                identifier();
            } else {
                Lox.error(line, "Unexpected character.");
            }

            break;
        }
    }

    /**
     * Tokenize identifier & add it to token list
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        // Check if identifier is a reserved word
        TokenType type = keywords.get(text);

        // ...if not, its a normal identifier
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }

    /**
     * Tokenize string literal & add to token list
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // We support multiline strings
            if (peek() == '\n') {
                line++;
                advance();
            }

            // Unterminated string
            if (isAtEnd()) {
                Lox.error(line, "Unterminated string.");
                return;
            }

            // The closing "
            advance();

            // Get value by trimming the surrounding quotes
            String value = source.substring(start + 1, current - 1);
            addToken(STRING, value);
        }
    }

    /**
     * Tokenize number literal & add to token list
     */
    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble((source.substring(start, current))));
    }

    // Helper methods

    /**
     * Consume the character. Advances <b>current</b>!
     *
     * @return The char at the OLD <b>current</b> position
     */
    private char advance() {
        current++;

        return source.charAt(current - 1);
    }

    /**
     * Think of a 'conditional advance()': Only consumes current char if it's what
     * we're looking for. Advances <b>current</b> if successful
     *
     * @param expected The value we expect for the current char
     */
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;

        return true;
    }

    /**
     * Similar to advance, but does not consume the character. Operates on
     * <b>current</b>, which is already ++ed at this point!
     *
     * @return Either the char at <b>current</b> or \0
     */
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    /**
     * Peeks one char after <b>current</b>, which is already ++ed at this point!
     *
     * @return Either the char at <b>current</b> + 1 or \0
     */
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private boolean isAlpha(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_';
    }

    private boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isAlphaNumeric(char character) {
        return isAlpha(character) || isDigit(character);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Adds token without literal value to token list
     *
     * @param type TokenType
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adds token to token list
     *
     * @param type    TokenType
     * @param literal Literal token value
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
