package util.error;

public class TypeAnnotationError extends TypeError {
    private String var;

    public TypeAnnotationError(int line, String var) {
        super(line);
        this.var = var;
    }

    @Override
    public String toString() {
        return super.toString() + "Missing type annotation for variable "+var+".";
    }
    
}
