package util.error;

public class NoReturnTypeError extends TypeError {
    private String name;

    public NoReturnTypeError(int line, String name) {
        super(line);
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + "Missing return type for method"+name+".";
    }
    
}
