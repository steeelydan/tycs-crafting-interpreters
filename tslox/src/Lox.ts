import { Scanner } from './Scanner/Scanner.js';

export class Lox {
    static hadError: boolean = false;

    static run(sourcecode: string) {
        const scanner = new Scanner(sourcecode);
        const tokens = scanner.scan();

        if (Lox.hadError) {
            process.exit(1);
        }
    }

    static error(lineNumber: number, message: string) {
        console.error(`[line ${lineNumber}] Error: ${message}`);

        this.hadError = true;
    }
}
