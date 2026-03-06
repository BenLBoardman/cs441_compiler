package util.error;

import parser.ASTMethod;
import util.DataType;

public class ReturnMismatchError extends TypeError {
    private ASTMethod method;
    private DataType type;
    public ReturnMismatchError(int line, ASTMethod method, DataType type) {
        super(line);
        this.method = method;
        this.type = type;
    }

    @Override
    public String toString() {
        return super.toString()+"Method "
        +method.classname()+"."+method.name()+
        " should return a value of type "+method.returnType()+
        ", not a value of type "+type+".";
    }
    
}
