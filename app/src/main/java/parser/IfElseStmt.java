package parser;

import java.util.ArrayList;

public record IfElseStmt(Expression cond, ArrayList<Statement> body, ArrayList<Statement> elseBody) implements Statement{}
