import { Token, TokenType } from '../types.js';
import { KEYWORDS } from '../constants.js';
import { Lox } from '../Lox.js';

export class Scanner {
    private source: string;
    private tokens: Token[] = [];
    private start: number = 0;
    private current: number = 0;
    private line: number = 1;

    constructor(sourcecode: string) {
        this.source = sourcecode;
    }

    scan() {
        this.scanTokens();
    }

    private scanTokens() {
        while (!this.isAtEnd()) {
            this.start = this.current;
            const c = this.advance();

            switch (c) {
                case '(': {
                    this.addToken('LEFT_PAREN');
                    break;
                }
                case ')': {
                    this.addToken('RIGHT_PAREN');
                    break;
                }
                case '{': {
                    this.addToken('LEFT_BRACE');
                    break;
                }
                case '}': {
                    this.addToken('RIGHT_BRACE');
                    break;
                }
                case ',': {
                    this.addToken('COMMA');
                    break;
                }
                case '.': {
                    this.addToken('DOT');
                    break;
                }
                case '-': {
                    this.addToken('MINUS');
                    break;
                }
                case '+': {
                    this.addToken('PLUS');
                    break;
                }
                case ';': {
                    this.addToken('SEMICOLON');
                    break;
                }
                case '*': {
                    this.addToken('STAR');
                    break;
                }
                // Operators with possibly 2 chars
                case '!': {
                    this.addToken(this.matchNext('=') ? 'BANG_EQUAL' : 'BANG');
                    break;
                }
                case '=': {
                    this.addToken(
                        this.matchNext('=') ? 'EQUAL_EQUAL' : 'EQUAL'
                    );
                    break;
                }
                case '<': {
                    this.addToken(this.matchNext('=') ? 'LESS_EQUAL' : 'LESS');
                    break;
                }
                case '>': {
                    this.addToken(
                        this.matchNext('=') ? 'GREATER_EQUAL' : 'GREATER'
                    );
                    break;
                }
                // Division & comments
                case '/': {
                    if (this.matchNext('/')) {
                        while (this.peek() != '\n' && !this.isAtEnd()) {
                            this.advance();
                        }
                    } else {
                        this.addToken('SLASH');
                    }
                    this.addToken(this.matchNext('=') ? 'BANG_EQUAL' : 'BANG');
                    break;
                }
                // Whitespace: Ignore
                case ' ': {
                    break;
                }
                case '\r': {
                    break;
                }
                case '\t': {
                    break;
                }
                case '\n': {
                    this.line++;
                    break;
                }
                // Strings
                case '"': {
                    this.string();
                    break;
                }
                default: {
                    // Numbers
                    if (this.isDigit(c)) {
                        this.number();
                    } else if (this.isAlpha(c)) {
                        this.identifier();
                    } else {
                        Lox.error(
                            this.line,
                            'Unexpected character: ' +
                                this.source.charAt(this.start)
                        );
                        break;
                    }
                }
            }
        }

        console.log('Tokens', this.tokens);
    }

    private identifier() {
        while (this.isAlphaNumeric(this.peek())) {
            this.advance();
        }

        const literal = this.source.substring(this.start, this.current);

        let tokenType = KEYWORDS[literal];

        if (!tokenType) {
            tokenType = 'IDENTIFIER';
        }

        this.addToken(tokenType);
    }

    private number() {
        while (this.isDigit(this.peek())) {
            this.advance();
        }

        if (this.peek() === '.' && this.isDigit(this.peekNext())) {
            this.advance();

            while (this.isDigit(this.peek())) {
                this.advance();
            }
        }

        this.addToken(
            'NUMBER',
            parseFloat(this.source.substring(this.start, this.current))
        );
    }

    private string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            if (this.peek() == '\n') {
                this.line++;
            }

            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(this.line, 'Unterminated string.');
            return;
        }

        this.advance();

        // Trim quotes to get actual value for later
        const literal = this.source.substring(this.start + 1, this.current - 1);

        this.addToken('STRING', literal);
    }

    private isAtEnd() {
        return this.current >= this.source.length;
    }

    private advance() {
        return this.source.charAt(this.current++);
    }

    private addToken(
        type: TokenType,
        literal: {} | null = null // FIXME
    ) {
        const lexeme = this.source.substring(this.start, this.current);

        this.tokens.push({
            type,
            lexeme,
            literal,
            line: this.line,
        });
    }

    private matchNext(expected: string) {
        if (this.isAtEnd()) {
            return false;
        }

        if (this.source.charAt(this.current) != expected) {
            return false;
        }

        this.current++;

        return true;
    }

    private peek() {
        if (this.isAtEnd()) {
            return '\0';
        }

        return this.source.charAt(this.current);
    }

    private peekNext() {
        if (this.current + 1 >= this.source.length) {
            return '\0';
        }

        return this.source.charAt(this.current + 1);
    }

    private isAlpha(c: string) {
        return /[a-zA-Z_]/.test(c);
    }

    private isAlphaNumeric(c: string) {
        return this.isAlpha(c) || this.isDigit(c);
    }

    private isDigit(c: string) {
        return c >= '0' && c <= '9';
    }
}
