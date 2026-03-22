package util.error.syntax;

import util.error.Error;

public abstract class SyntaxError extends Error {

    public SyntaxError(int line) {
        super(line);
    }
    
    @Override
    public String toString() {
        return super.toString() + "Syntax Error: ";
    }
}
