package util.error.syntax;


public class MethodSyntaxError extends SyntaxError {
    private SyntaxErrorType type;
    private String name;
    
    public MethodSyntaxError(int line, String name, SyntaxErrorType type) {
        super(line);
        this.type = type;
        this.name = name;
        switch (type) {
            default:
                break;
        }
        
    }

    @Override
    public String toString() {
        switch (type) {
            case NORETURN:
                return super.toString() + "Missing return type for method "+name+".";
            case MISSING_WITH:
                return super.toString() + "Missing token \"with\" in definition for method "+name+".";
            case MISSING_LOCALS:
                return super.toString() + "Missing token \"locals\" in definition for method "+name+".";
            default:
                return "Unknown syntax error in definition for method "+name+".";
        }
        
    }
    
}
