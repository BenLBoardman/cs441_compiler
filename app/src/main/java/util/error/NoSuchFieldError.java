package util.error;

public class NoSuchFieldError extends Error {
    private String className;
    private String field;
    public NoSuchFieldError(int line, String className, String field) {
        super(line);
        this.className = className;
        this.field = field;
    }

    @Override
    public String toString() {
        return super.toString() + "No Such Field Error: class "+className+" has no field named "+field+".";
    }
    
}
