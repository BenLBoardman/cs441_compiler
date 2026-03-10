package util.error.syntax;


public class TypeAnnotationError extends SyntaxError {
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
