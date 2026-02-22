package parser;

import java.util.ArrayList;
import java.util.HashMap;
import tokenize.*;
import util.DataType;

public class Parser {
    private Tokenizer tok;
    public Parser(Tokenizer t) {
        tok = t;
    }

    public Expression parseExpr() {
        switch (tok.next()) {
            case Eof eof: throw new IllegalArgumentException("No expression to parse: EOF");
            case NumberTok n: return new Constant(n.value());
            case Identifier i: return new Variable(i.name());
            case LeftParen p:
                // Should be start of a binary operation
                Expression lhs = parseExpr();
                Token optok = tok.next();
                if (optok.getType() != TokenType.OPERATOR || ((Operator)optok).getOp().equals("="))
                    throw new IllegalArgumentException("Expected non-assignment operator but found "+optok);
                Expression rhs = parseExpr();

                Token closetok = tok.next();
                if (closetok.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected right paren but found "+closetok);
                Binop op = new Binop(lhs, ((Operator)optok).op(), rhs);
                if(!op.isBool() && (lhs instanceof ThisExpr || rhs instanceof ThisExpr)) {
                    throw new IllegalArgumentException("Error: Math operations cannot be performed on \"this\"");
                }
                return op;
            case Ampersand a:
                // Should be field read
                Expression base = parseExpr();
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found "+dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found "+fname);
                return new FieldRead(base, ((Identifier)fname).name());
            case Caret c:
                // Should be method call
                Expression mbase = parseExpr();
                Token mdot = tok.next();
                if (mdot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found "+mdot);
                Token mname = tok.next();
                if (mname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid method name but found "+mname);
                Token open = tok.next();
                if (open.getType() != TokenType.LEFT_PAREN)
                    throw new IllegalArgumentException("Expected left paren but found "+open);
                // Now we iterate through arguments
                ArrayList<Expression> args = new ArrayList<>();
                while (tok.peek().getType() != TokenType.RIGHT_PAREN) {
                    Expression e = parseExpr();
                    args.add(e);
                    // Now either a paren or a comma
                    Token punc = tok.peek();
                    if (punc.getType() == TokenType.COMMA)
                        tok.next(); // throw away the comma
                    else if(punc.getType() != TokenType.RIGHT_PAREN)
                        throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
                }
                tok.next(); //throw away right-paren
                return new MethodCall(mbase, ((Identifier)mname).name(), args);
            case AtSign a:
                Token cname = tok.next();
                if (cname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid class name but found: "+cname);
                return new ClassRef(((Identifier)cname).name());
            case This t: return new ThisExpr();
            case NullTok n:
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following null token ");
            return new NullExpr(DataType.processType(tok.next()));
            case Token o:
                throw new IllegalArgumentException("Token "+o+" is not a valid start of an expression");
        }
    }

    public Statement parseStmt() {
        Token eql, colon, lbrace;
        Expression cond;
        ArrayList<Statement> ifBody;
        switch(tok.peek()) {
            case Underscore u: //void: _ = <expr>
                tok.next();
                eql = tok.next();
                    if(eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                        throw new IllegalArgumentException("Expected '=' but found "+eql);
                return new VoidStmt(parseExpr());
            case Not n: //Field write !expr.field = expr
                tok.next();
                Expression base = parseExpr();
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found " + dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found " + fname);
                eql = tok.next();
                if (eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                    throw new IllegalArgumentException("Expected '=' but found " + eql);
                Expression rhs = parseExpr();
                return new FieldWriteStmt(base, ((Identifier) fname).name(), rhs);
            case If i: //if e: { <newline> <one or more statements> } else { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if (colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found " + colon);
                lbrace = tok.next();
                if (lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found " + lbrace);
                ifBody = new ArrayList<Statement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt());
                    if (tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }
                tok.next(); // throw out right brace
                Token elseT = tok.next();
                if (elseT.getType() != TokenType.ELSE)
                    throw new IllegalArgumentException("Expected 'else' but found " + elseT);
                lbrace = tok.next();
                if (lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found " + lbrace);
                ArrayList<Statement> elseBody = new ArrayList<Statement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    elseBody.add(parseStmt());
                    if (tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing else statement");
                }
                tok.next(); // throw out right brace
                return new IfElseStmt(cond, ifBody, elseBody);
            case IfOnly i: //ifonly e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ifBody = new ArrayList<Statement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt());
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new IfOnlyStmt(cond, ifBody); 
            case While w: //while e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ArrayList<Statement> body = new ArrayList<Statement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    body.add(parseStmt());
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new WhileStmt(cond, body);
            case Return r: //return: return(expr)
                tok.next();
                Expression out = parseExpr();
                return new ReturnStmt(out);
            case Print p:
                tok.next(); //print: print(expr)
                Token lparen = tok.next();
                if(lparen.getType() != TokenType.LEFT_PAREN)
                    throw new IllegalArgumentException("Expected '(' but found "+lparen);
                Expression prt = parseExpr();
                Token rparen = tok.next();
                if(rparen.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected ')' but found "+rparen);
                return new PrintStmt(prt);
            default: //nothing else, check if its a start of an expression and see if its a variable assignment
                switch (parseExpr()) {
                    case Variable v: // assignment: v = <expr>
                        eql = tok.next();
                        if(eql.getType() != TokenType.OPERATOR || !((Operator) eql).getOp().equals("="))
                            throw new IllegalArgumentException("Expected '=' but found "+eql);
                        return new AssignStmt(v, parseExpr());
                    default:
                        throw new IllegalArgumentException("Could not find valid statement or expression");
                }
        }
    }

    public Method parseMethod() {
        Token method = tok.next();
        if(method.getType() != TokenType.METHOD)
            throw new IllegalArgumentException("Expected 'method', found"+method);
        Token nameTok = tok.next();
        if(nameTok.getType() != TokenType.IDENTIFIER)
            throw new IllegalArgumentException(nameTok+" is not a valid method name");
        String name = ((Identifier)nameTok).name();
        Token lparen = tok.next();
        if(lparen.getType() != TokenType.LEFT_PAREN)
            throw new IllegalArgumentException("Expected '(', found"+lparen);
        HashMap<String, DataType> args = new HashMap<>();
        String argName;
        DataType argType;
        while(tok.peek().getType() != TokenType.RIGHT_PAREN) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing arg "+(args.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            argName = ((Identifier)id).name();
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following method arg "+argName);
            argType = DataType.processType(tok.next());
            args.put(argName, argType);
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.RIGHT_PAREN)
                throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
        }
        tok.next(); //throw out right paren
        
        //check for return datatype
        DataType returnType;
        Token ret = tok.next();
        if(ret.getType() != TokenType.RETURNING) 
            throw new IllegalArgumentException("Expected \"returning\" type statement, found"+ret);
        Token type = tok.next();
        returnType = DataType.processType(type); //TODO - test if thrown exception in submethod will work 
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        Token tLocals = tok.next();
        if(tLocals.getType() != TokenType.LOCALS) 
            throw new IllegalArgumentException("Expected 'locals', found"+tLocals);
        HashMap<String, DataType> locals = new HashMap<>();
        String locName;
        DataType locType;
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing local "+(locals.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            locName = ((Identifier)id).name();
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following method arg "+locName);
            locType = DataType.processType(tok.next());
            locals.put(locName, locType);
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.COLON)
                throw new IllegalArgumentException("Expected either ',' or ':', found "+punc);
        }
        tok.next(); //throw away colon
        ArrayList<Statement> body = new ArrayList<>();
        while(tok.peek().getType() != TokenType.METHOD
            && tok.peek().getType() != TokenType.EOF && tok.peek().getType() != TokenType.RIGHT_BRACK) {
            try {
                body.add(parseStmt());
                if(tok.peek().getType() == TokenType.RETURN) { //unconditional return
                    body.add(parseStmt());
                    break;
                }
            } catch(IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing statement "+(body.size()+1)+" of method "+name+": "+e.getMessage());
            }
        }
        return new Method(name, args, returnType, locals, body);
    }

    public Method parseMain() {
        Token method = tok.next();
        if(method.getType() != TokenType.MAIN)
            throw new IllegalArgumentException("Expected 'main', found"+method);
        String name = "main";
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        HashMap<String, DataType> locals = new HashMap<>();
        String locName;
        DataType locType;
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            locName = ((Identifier)id).name();
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following method arg "+locName);
            locType = DataType.processType(tok.next());
            locals.put(locName, locType);
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.COLON)
                throw new IllegalArgumentException("Expected either ',' or ':', found "+punc);
        }
        tok.next(); //throw away colon
        ArrayList<Statement> body = new ArrayList<>();
        while(tok.peek().getType() != TokenType.EOF) {
            body.add(parseStmt());
        }
        body.add(new ReturnStmt(new Constant(0)));
        return new Method(name, new HashMap<>(), DataType.getType(new Int()), locals, body);
    }

    public ParserClass parseClass () {
        Token tClass = tok.next();
        if(tClass.getType() != TokenType.CLASS)
            throw new IllegalArgumentException("Expected top-level class declaration, found"+tClass);
        Token nameTok = tok.next();
        if(nameTok.getType() != TokenType.IDENTIFIER)
            throw new IllegalArgumentException(nameTok+" is not a valid class name");
        String name = ((Identifier)nameTok).name();
        Token lbrack = tok.next();
        if(lbrack.getType() != TokenType.LEFT_BRACK)
            throw new IllegalArgumentException("Expected '[', found"+lbrack);
        Token tFields = tok.next();
        if(tFields.getType() != TokenType.FIELDS) 
            throw new IllegalArgumentException("Expected 'fields', found"+tFields);
        HashMap<String, DataType> fields = new HashMap<>();
        String fieldName;
        DataType fieldType;
        while(tok.peek().getType() != TokenType.METHOD) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            fieldName = ((Identifier)id).name();
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following field name "+fieldName);
            fieldType = DataType.processType(tok.next());
            fields.put(fieldName, fieldType);
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.METHOD)
                throw new IllegalArgumentException("Expected either ',' or 'method', found "+punc);
        }
        HashMap<Method, DataType> methods = new HashMap<>();
        Method method;
        while(tok.peek().getType() != TokenType.RIGHT_BRACK) {
            method = parseMethod();
            methods.put(method, method.returnType());
        } 
        tok.next(); //throw away right bracket
        return new ParserClass(name, fields, DataType.processType(nameTok), methods);
    }

    public ParsedCode parse() { // parse EVERYTHING in the input as a series of Classes
        ArrayList<String> typeNames = new ArrayList<>(); //list of declared types
        ArrayList<ParserClass> classes = new ArrayList<>();

        typeNames.add("int");
        while (tok.peek().getType() != TokenType.MAIN) {
            classes.add(parseClass());
            typeNames.add(classes.get(classes.size()-1).name());
        }
        for(ParserClass c : classes) {
            for(Method m : c.iterMethods()) {
                m.checkTypes(typeNames); //check that all vars use types that exist
            }
        }

        Method main = parseMain(); //check that all vars use types that exist
        main.checkTypes(typeNames);
        return new ParsedCode(main, classes);
    }
}
