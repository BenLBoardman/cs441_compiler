package util.error.type;

import util.error.Error;

public abstract class TypeError extends Error {

    public TypeError(int line) {
        super(line);
    }
    
    @Override
    public String toString() {
        return super.toString() + "Type error: ";
    }
}