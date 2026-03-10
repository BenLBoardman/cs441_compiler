package util.error.type;

import util.DataType;

public class UndefinedClassError extends TypeError {
    private DataType classType;
    
    public UndefinedClassError(int line, DataType classType) {
        super(line);
        this.classType = classType;
    }

    @Override
    public String toString() {
        return super.toString()+" Reference to undefined class "+classType.typeName()+".";
    }
    
}
