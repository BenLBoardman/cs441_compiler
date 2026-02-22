package parser;

public sealed interface Expression 
    permits Constant, Binop, MethodCall, FieldRead, ClassRef, ThisExpr, Variable, NullExpr {
}
