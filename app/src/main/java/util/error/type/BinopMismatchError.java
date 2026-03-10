package util.error.type;

import util.DataType;

public class BinopMismatchError extends TypeError {
    private DataType left, right;
    public BinopMismatchError(int line, DataType left, DataType right) {
        super(line);
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return super.toString() + "Type mismatch in binary operation (left: "+left+", right: "+right+").";
    }
    
}
