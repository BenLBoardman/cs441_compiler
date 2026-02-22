package parser;

import java.util.ArrayList;

public record IfOnlyStmt(Expression cond, ArrayList<Statement> body) implements Statement{}
