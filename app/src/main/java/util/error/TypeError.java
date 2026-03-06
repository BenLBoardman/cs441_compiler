package util.error;

public abstract class TypeError extends Error {

    public TypeError(int line) {
        super(line);
    }
    
    @Override
    public String toString() {
        return super.toString() + "Type error: ";
    }
}