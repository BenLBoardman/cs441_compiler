package util.error;


public abstract class Error {
    private int line;

    public Error(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "Error on line "+line+": ";
    }
}
