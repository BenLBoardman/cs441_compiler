package util.error;

import util.DataType;

public class AssignmentTypeError extends TypeError {
    private DataType var, right;

    public AssignmentTypeError(int line, DataType var, DataType right) {
        super(line);
        this.var = var;
        this.right = right;
    }

    @Override
    public String toString() {
        return super.toString() + "Attempt to assign value of type"+var+"to variable of type"+right+".";
    }
    
}
