package parser;

import java.util.ArrayList;
import java.util.HashMap;

import parser.expression.*;
import parser.statement.*;
import tokenize.*;
import tokenize.token.*;
import util.DataType;

public class Parser {
    private Tokenizer tok;
    public Parser(Tokenizer t) {
        tok = t;
    }

    public ASTExpression parseExpr(ASTMethod method) {
        switch (tok.next()) {
            case Eof eof: throw new IllegalArgumentException("No expression to parse: EOF");
            case NumberTok n: return new ASTConstant(n.value());
            case Identifier i: return new ASTVariable(i.name());
            case LeftParen p:
                // Should be start of a binary operation
                ASTExpression lhs = parseExpr(method);
                Token optok = tok.next();
                if (optok.getType() != TokenType.OPERATOR || ((Operator)optok).getOp().equals("="))
                    throw new IllegalArgumentException("Expected non-assignment operator but found "+optok);
                ASTExpression rhs = parseExpr(method);

                Token closetok = tok.next();
                if (closetok.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected right paren but found "+closetok);
                ASTBinop op = new ASTBinop(lhs, ((Operator)optok).op(), rhs);
                if(!op.isBool() && (lhs instanceof ASTThisExpr || rhs instanceof ASTThisExpr)) {
                    throw new IllegalArgumentException("Error: Math operations cannot be performed on \"this\"");
                }
                return op;
            case Ampersand a:
                // Should be field read
                ASTExpression base = parseExpr(method);
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found "+dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found "+fname);
                return new ASTFieldRead(base, ((Identifier)fname).name());
            case Caret c:
                // Should be method call
                ASTExpression mbase = parseExpr(method);
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
                ArrayList<ASTExpression> args = new ArrayList<>();
                while (tok.peek().getType() != TokenType.RIGHT_PAREN) {
                    ASTExpression e = parseExpr(method);
                    args.add(e);
                    // Now either a paren or a comma
                    Token punc = tok.peek();
                    if (punc.getType() == TokenType.COMMA)
                        tok.next(); // throw away the comma
                    else if(punc.getType() != TokenType.RIGHT_PAREN)
                        throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
                }
                tok.next(); //throw away right-paren
                return new ASTMethodCall(mbase, ((Identifier)mname).name(), args);
            case AtSign a:
                Token cname = tok.next();
                if (cname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid class name but found: "+cname);
                return new ASTClassRef(((Identifier)cname).name());
            case This t: 
                return new ASTThisExpr(method.classname());
            case NullTok n:
            if(tok.next().getType() != TokenType.COLON)
                throw new IllegalArgumentException("Error: Expected colon for type annotation following null token ");
            return new ASTNullExpr(DataType.processType(tok.next()));
            case Token o:
                throw new IllegalArgumentException("Token "+o+" is not a valid start of an expression");
        }
    }

    public ASTStatement parseStmt(ASTMethod method) {
        Token eql, colon, lbrace;
        ASTExpression cond;
        ArrayList<ASTStatement> ifBody;
        switch(tok.peek()) {
            case Underscore u: //void: _ = <expr>
                tok.next();
                eql = tok.next();
                    if(eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                        throw new IllegalArgumentException("Expected '=' but found "+eql);
                return new ASTVoidStmt(parseExpr(method));
            case Not n: //Field write !expr.field = expr
                tok.next();
                ASTExpression base = parseExpr(method);
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found " + dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found " + fname);
                eql = tok.next();
                if (eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                    throw new IllegalArgumentException("Expected '=' but found " + eql);
                ASTExpression rhs = parseExpr(method);
                return new ASTFieldWriteStmt(base, ((Identifier) fname).name(), rhs);
            case If i: //if e: { <newline> <one or more statements> } else { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr(method);
                colon = tok.next();
                if (colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found " + colon);
                lbrace = tok.next();
                if (lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found " + lbrace);
                ifBody = new ArrayList<ASTStatement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt(method));
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
                ArrayList<ASTStatement> elseBody = new ArrayList<ASTStatement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    elseBody.add(parseStmt(method));
                    if (tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing else statement");
                }
                tok.next(); // throw out right brace
                return new ASTIfElseStmt(cond, ifBody, elseBody);
            case IfOnly i: //ifonly e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr(method);
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ifBody = new ArrayList<ASTStatement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt(method));
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new ASTIfOnlyStmt(cond, ifBody); 
            case While w: //while e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr(method);
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ArrayList<ASTStatement> body = new ArrayList<ASTStatement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    body.add(parseStmt(method));
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new ASTWhileStmt(cond, body);
            case Return r: //return: return(expr)
                tok.next();
                ASTExpression out = parseExpr(method);
                return new ASTReturnStmt(out, method);
            case Print p:
                tok.next(); //print: print(expr)
                Token lparen = tok.next();
                if(lparen.getType() != TokenType.LEFT_PAREN)
                    throw new IllegalArgumentException("Expected '(' but found "+lparen);
                ASTExpression prt = parseExpr(method);
                Token rparen = tok.next();
                if(rparen.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected ')' but found "+rparen);
                return new ASTPrintStmt(prt);
            default: //nothing else, check if its a start of an expression and see if its a variable assignment
                switch (parseExpr(method)) {
                    case ASTVariable v: // assignment: v = <expr>
                        eql = tok.next();
                        if(eql.getType() != TokenType.OPERATOR || !((Operator) eql).getOp().equals("="))
                            throw new IllegalArgumentException("Expected '=' but found "+eql);
                        return new ASTAssignStmt(v, parseExpr(method));
                    default:
                        throw new IllegalArgumentException("Could not find valid statement or expression");
                }
        }
    }

    public ASTMethod parseMethod(String classname) {
        Token methodTok = tok.next();
        if(methodTok.getType() != TokenType.METHOD)
            throw new IllegalArgumentException("Expected 'method', found"+methodTok);
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
        ArrayList<ASTStatement> body = new ArrayList<>();
        ASTMethod method = new ASTMethod(name, classname, args, returnType, locals, body);
        while(tok.peek().getType() != TokenType.METHOD
            && tok.peek().getType() != TokenType.EOF && tok.peek().getType() != TokenType.RIGHT_BRACK) {
            try {
                body.add(parseStmt(method));
                if(tok.peek().getType() == TokenType.RETURN) { //unconditional return
                    body.add(parseStmt(method));
                    break;
                }
            } catch(IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing statement "+(body.size()+1)+" of method "+name+": "+e.getMessage());
            }
        }
        return method;
    }

    public ASTMethod parseMain() {
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
        ArrayList<ASTStatement> body = new ArrayList<>();
        ASTMethod main = new ASTMethod(name, "", new HashMap<>(), DataType.getType(new Int()), locals, body);
        while(tok.peek().getType() != TokenType.EOF) {
            body.add(parseStmt(main));
        }
        body.add(new ASTReturnStmt(new ASTConstant(0), main));
        return main;
    }

    public ASTClass parseClass () {
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
        HashMap<String, ASTMethod> methods = new HashMap<>();
        ASTMethod method;
        while(tok.peek().getType() != TokenType.RIGHT_BRACK) {
            method = parseMethod(name);
            methods.put(method.name(), method);
        } 
        tok.next(); //throw away right bracket
        return new ASTClass(name, fields, DataType.processType(nameTok), methods);
    }

    public ParsedCode parse() { // parse EVERYTHING in the input as a series of Classes
        HashMap<String, ASTClass> types = new HashMap<>(); //list of declared types
        ArrayList<ASTClass> classes = new ArrayList<>();

        types.put("int", new ASTClass("int", null, DataType.intType, null)); //dummy class for ints
        ASTClass astClass;
        while (tok.peek().getType() != TokenType.MAIN) {
            astClass = parseClass();
            classes.add(astClass);
            types.put(astClass.name(), astClass);
        }
        for(ASTClass c : classes) {
            for(ASTMethod m : c.iterMethods()) {
                m.checkTypes(types); //check that all vars use types that exist
            }
        }

        ASTMethod main = parseMain(); //check that all vars use types that exist
        main.checkTypes(types);
        return new ParsedCode(main, classes);
    }
}
