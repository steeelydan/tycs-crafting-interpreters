import fs from 'fs';
import path from 'path';

const loxSource = fs.readFileSync(path.resolve('demo.lox'), 'utf-8');

console.log(loxSource);
