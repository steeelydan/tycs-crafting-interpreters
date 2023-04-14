import fs from 'fs';
import path from 'path';
import { Lox } from './Lox.js';

const sourcecode = fs.readFileSync(path.resolve('demo.lox'), 'utf-8');

Lox.run(sourcecode);
