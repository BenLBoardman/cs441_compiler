package util;

public abstract class Error {
    private int line;
    private CompilePhase phase;

    public Error(int line, CompilePhase phase) {
        this.line = line;
        this.phase = phase;
    }

    @Override
    public String toString() {
        return "Error on line "+line+": "+phase+"error:";
    }
}
