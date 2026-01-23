import java.lang.StringBuilder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;


enum TokenType { 
    // Fixed punctuation
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACK,
    RIGHT_BRACK,
    CARET,
    AMPERSAND,
    ATSIGN,
    NOT,
    DOT,
    COLON,
    COMMA,
    UNDERSCORE,
    // Keywords
    THIS,
    IF,
    IFONLY,
    ELSE,
    WHILE,
    RETURN,
    METHOD,
    CLASS,
    PRINT,
    FIELDS,
    WITH,
    LOCALS,
    MAIN,
    EOF,
    // Tokens with data
    OPERATOR,
    NUMBER,
    IDENTIFIER
}

sealed interface Token 
    permits Number, LeftParen, RightParen, Operator, Caret, Ampersand, AtSign, Not, Dot, Underscore, If, IfOnly, Else, While, Return, TMethod, TClass, Print, 
    Fields, With, Locals, Main, Colon, LeftBrace, RightBrace, LeftBrack, RightBrack, Identifier, Eof, This, Comma {
    TokenType getType();
}
record Number(long value) implements Token { @Override public TokenType getType() { return TokenType.NUMBER; } }
record LeftParen() implements Token { @Override public TokenType getType() { return TokenType.LEFT_PAREN; } }
record RightParen() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_PAREN; } }
record LeftBrace() implements Token { @Override public TokenType getType() { return TokenType.LEFT_BRACE; } }
record RightBrace() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_BRACE; } }
record LeftBrack() implements Token { @Override public TokenType getType() { return TokenType.LEFT_BRACK; } }
record RightBrack() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_BRACK; } }
record Operator(String op) implements Token {
    @Override public TokenType getType() { return TokenType.OPERATOR; }
    public String getOp() { return this.op; }
}
record Caret() implements Token { @Override public TokenType getType() { return TokenType.CARET; } }
record Ampersand() implements Token { @Override public TokenType getType() { return TokenType.AMPERSAND; } }
record AtSign() implements Token { @Override public TokenType getType() { return TokenType.ATSIGN; } }
record Not() implements Token { @Override public TokenType getType() { return TokenType.NOT; } }
record Dot() implements Token { @Override public TokenType getType() { return TokenType.DOT; } }
record Underscore() implements Token { @Override public TokenType getType() { return TokenType.UNDERSCORE; } }
record If() implements Token { @Override public TokenType getType() { return TokenType.IF; } }
record IfOnly() implements Token { @Override public TokenType getType() { return TokenType.IFONLY; } }
record Else() implements Token { @Override public TokenType getType() { return TokenType.ELSE; } }
record While() implements Token { @Override public TokenType getType() { return TokenType.WHILE; } }
record Return() implements Token { @Override public TokenType getType() { return TokenType.RETURN; } }
record TMethod() implements Token { @Override public TokenType getType() { return TokenType.METHOD; } }
record TClass() implements Token { @Override public TokenType getType() { return TokenType.CLASS; } }
record Print() implements Token { @Override public TokenType getType() { return TokenType.PRINT; } }
record With() implements Token { @Override public TokenType getType() { return TokenType.WITH; } }
record Fields() implements Token { @Override public TokenType getType() { return TokenType.FIELDS; } }
record Main() implements Token { @Override public TokenType getType() { return TokenType.MAIN; } }
record Locals() implements Token { @Override public TokenType getType() { return TokenType.LOCALS; } }
record Colon() implements Token { @Override public TokenType getType() { return TokenType.COLON; } }
record Comma() implements Token { @Override public TokenType getType() { return TokenType.COMMA; } }
record Eof() implements Token { @Override public TokenType getType() { return TokenType.EOF; } }
record Identifier(String name) implements Token { @Override public TokenType getType() { return TokenType.IDENTIFIER; } }
record This() implements Token { @Override public TokenType getType() { return TokenType.THIS; } }

class Tokenizer {

    // We'll pre-allocate and reuse common tokens without data
    private final LeftParen lp = new LeftParen();
    private final RightParen rp = new RightParen();
    private final LeftBrace lb = new LeftBrace();
    private final RightBrace rb = new RightBrace();
    private final LeftBrack lbk = new LeftBrack();
    private final RightBrack rbk = new RightBrack();
    private final Colon colon = new Colon();
    private final TMethod method = new TMethod();
    private final TClass tClass = new TClass();
    private final Print print = new Print();
    private final With with = new With();
    private final Fields fields = new Fields();
    private final Main main = new Main();
    private final Locals locals = new Locals();
    private final Return ret = new Return();
    private final While w = new While();
    private final If iff = new If();
    private final IfOnly ifonly = new IfOnly();
    private final Else elseTok = new Else();
    private final Not not = new Not();
    private final AtSign at = new AtSign();
    private final Underscore underscore = new Underscore();
    private final Caret caret = new Caret();
    private final Ampersand amp = new Ampersand();
    private final Dot dot = new Dot();
    private final Eof eof = new Eof();
    private final This th = new This();
    private final Comma comma = new Comma();

    private final String text;
    private int current;
    private Token cached;
    public Tokenizer(String t) {
        this.text = t;
        this.current = 0;
    }
    public Token peek() {
        if (cached == null) {
            cached = advanceCurrent();
        }
        return cached;
    }
    public Token next() {
        if (cached == null) {
            return advanceCurrent();
        } else {
            Token tmp = cached;
            cached = null;
            return tmp;
        }
    }
    private Token advanceCurrent() {
        while (current < text.length() && Character.isWhitespace(text.charAt(current))) {
            current++;
        }
        if (current >= text.length()) {
            return this.eof;
        }
        switch (text.charAt(current)) {
            case '(': current++; return lp;
            case ')': current++; return rp;
            case '{': current++; return lb;
            case '}': current++; return rb;
            case '[': current++; return lbk;
            case ']': current++; return rbk;
            case ':': current++; return colon;
            case '!': current++; return not;
            case '@': current++; return at;
            case '^': current++; return caret;
            case '&': current++; return amp;
            case '.': current++; return dot;
            case ',': current++; return comma;
            case '_': current++; return underscore;

            case '<': 
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator("<");
                current++;
                return new Operator("<=");
            case '>':
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator(">");
                current++;
                return new Operator(">=");
            case '+': current++; return new Operator("+");
            case '-': current++; return new Operator("-");
            case '*': current++; return new Operator("*");
            case '/': current++; return new Operator("/");
            case '=': 
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator("=");
                current++;
                return new Operator("==");
            
            default:
                if (Character.isDigit(text.charAt(current))) {
                    // This is a digit
                    int start = current++;
                    while (current < text.length() && Character.isDigit(text.charAt(current))) current ++;
                    // current now points to the first non-digit character, or past the end of the text
                    return new Number(Integer.parseInt(text.substring(start,current)));
                }
                // Now down to keywords and identifiers
                else if (Character.isLetter(text.charAt(current))) {
                    int start = current++;
                    while (current < text.length() && Character.isLetterOrDigit(text.charAt(current))) current ++;
                    // current now points to the first non-alphanumeric character, or past the end of the string
                    String fragment = text.substring(start,current);
                    // Unlike the constant parsing switch above, this has already advanced current
                    switch (fragment) {
                        case "if": return iff;
                        case "ifonly": return ifonly;
                        case "else": return elseTok;
                        case "while": return w;
                        case "return": return ret;
                        case "method": return method;
                        case "class": return tClass;
                        case "print": return print;
                        case "this": return th;
                        case "with": return with;
                        case "fields": return fields;
                        case "main": return main;
                        case "locals": return locals;
                        default: return new Identifier(fragment);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported character: "+text.charAt(current));
                }
        }
    }
}

//Expressions
sealed interface Expression 
    permits Constant, Binop, MethodCall, FieldRead, ClassRef, ThisExpr, Variable {
    // TODO: You'll need to provide your own methods for operations you care about
}
record ThisExpr() implements Expression {}
record Constant(long value) implements Expression {}
record Binop(Expression lhs, String op, Expression rhs) implements Expression {}
record MethodCall(Expression base, String methodname, List<Expression> args) implements Expression {}
record FieldRead(Expression base, String fieldname) implements Expression {}
record ClassRef(String classname) implements Expression {}
record Variable(String name) implements Expression {}


enum StatementType {
    ASSIGN,
    VOID,
    FIELDWRITE,
    IFELSE,
    IFONLY,
    WHILE,
    RETURN,
    PRINT
}
//Statements
sealed interface Statement
    permits AssignStmt, VoidStmt, FieldWriteStmt, IfElseStmt, IfOnlyStmt, WhileStmt, ReturnStmt, PrintStmt {
        public StatementType getType();
}
record AssignStmt(Variable var, Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.ASSIGN; } }
record VoidStmt(Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.VOID; } }
record FieldWriteStmt(Expression base, String fieldname, Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.FIELDWRITE; } }
record IfElseStmt(Expression cond, ArrayList<Statement> body, ArrayList<Statement> elseBody) implements Statement{ @Override public StatementType getType(){ return StatementType.IFELSE; } }
record IfOnlyStmt(Expression cond, ArrayList<Statement> body) implements Statement{ @Override public StatementType getType(){ return StatementType.IFONLY; } }
record WhileStmt(Expression cond, ArrayList<Statement> body) implements Statement{ @Override public StatementType getType(){ return StatementType.WHILE; } }
record ReturnStmt(Expression output) implements Statement{ @Override public StatementType getType(){ return StatementType.RETURN; } }
record PrintStmt(Expression str) implements Statement{ @Override public StatementType getType(){ return StatementType.PRINT; } }

//Method & Class
record Method(String name, ArrayList<String> args, ArrayList<String> locals, ArrayList<Statement> body){}
record Class(String name, ArrayList<String> fields, ArrayList<Method> methods){}

class ParsedCode {
    public final Method main;
    public final ArrayList<Class> classes;

    public ParsedCode(Method main, ArrayList<Class> classes) {
        this.main = main;
        this.classes = classes;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`````````````````````\n\tPARSED CODE\n");
        for (Class c : classes) {
            sb.append("CLASS\n");
            sb.append(c.toString() + "\n");
        }
        sb.append("MAIN\n");
        sb.append(main.toString());
        return sb.toString();
    }
}

class Parser {
    private Tokenizer tok;
    public Parser(Tokenizer t) {
        tok = t;
    }

    public Expression parseExpr() {
        switch (tok.next()) {
            case Eof eof: throw new IllegalArgumentException("No expression to parse: EOF");
            case Number n: return new Constant(n.value());
            case Identifier i: return new Variable(i.name());
            case LeftParen p:
                // Should be start of a binary operation
                Expression lhs = parseExpr();
                Token optok = tok.next();
                if (optok.getType() != TokenType.OPERATOR)
                    throw new IllegalArgumentException("Expected operator but found "+optok);
                Expression rhs = parseExpr();
                Token closetok = tok.next();
                if (closetok.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected right paren but found "+closetok);
                return new Binop(lhs, ((Operator)optok).op(), rhs);
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
                        if(eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
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
        ArrayList<String> args = new ArrayList<>();
        while(tok.peek().getType() != TokenType.RIGHT_PAREN) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing arg "+(args.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            args.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.RIGHT_PAREN)
                throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
        }
        tok.next(); //throw out right paren
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        Token tLocals = tok.next();
        if(tLocals.getType() != TokenType.LOCALS) 
            throw new IllegalArgumentException("Expected 'locals', found"+tLocals);
        ArrayList<String> locals = new ArrayList<>();
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing local "+(locals.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            locals.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.COLON)
                throw new IllegalArgumentException("Expected either ',' or ':', found "+punc);
        }
        tok.next(); //throw away colon
        ArrayList<Statement> body = new ArrayList<>();
        while(tok.peek().getType() != TokenType.METHOD && tok.peek().getType() != TokenType.EOF && tok.peek().getType() != TokenType.RIGHT_BRACK) {
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
        return new Method(name, args, locals, body);
    }

    public Method parseMain() {
        Token method = tok.next();
        if(method.getType() != TokenType.MAIN)
            throw new IllegalArgumentException("Expected 'main', found"+method);
        String name = "main";
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        ArrayList<String> locals = new ArrayList<>();
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            locals.add(((Identifier)id).name());
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
        return new Method(name, null, locals, body);
    }

    public Class parseClass () {
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
        ArrayList<String> fields = new ArrayList<>();
        while(tok.peek().getType() != TokenType.METHOD) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            fields.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.METHOD)
                throw new IllegalArgumentException("Expected either ',' or 'method', found "+punc);
        }
        ArrayList<Method> methods = new ArrayList<>();
        while(tok.peek().getType() != TokenType.RIGHT_BRACK) {
            methods.add(parseMethod());
        } 
        tok.next(); //throw away right bracket
        return new Class(name, fields, methods);
    }

    public ParsedCode parse() { // parse EVERYTHING in the input as a series of Classes
        ArrayList<Class> classes = new ArrayList<>();
        while (tok.peek().getType() != TokenType.MAIN) {
            classes.add(parseClass());
        }
        Method main = parseMain();
        return new ParsedCode(main, classes);
    }
}

enum CFGOpType {
    CFG_UNASSN,
    CFG_BIASSN,
    CFG_CALLASSN,
    CFG_PHIASSN,
    CFG_ALLOCASSN,
    CFG_PRINT,
    CFG_GETELEM,
    CFG_SETELEM,
    CFG_LOADADDR,
    CFG_STOREADDR
}
sealed interface CFGOp
    permits CFGUnaryAssn {
        public CFGOpType getType();
}
record CFGUnaryAssn(/*CFGVar var, CFGVal val*/) implements CFGOp { @Override public CFGOpType getType(){ return CFGOpType.CFG_UNASSN; } }


class BasicBlock {
    public static ArrayList<BasicBlock> controlFlowGraph;

    public void makeCFG(ArrayList<Class> codeClasses) {
        for(Class c : codeClasses) {
            for(Method m : c.methods()) {
                if(m.name().equals("main"));
            }
        }
    }
}



public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <comp> {tokenize|parseExpr} [args...]");
            System.exit(1);
        }

        // This is just some code to kick the tires on the tokenizer, your compiler has no need to do this
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" "); // Without this, separate identifiers will run together
        }
        Tokenizer tok = new Tokenizer(sb.toString());
        Parser p;
        switch (args[0]) {
            case "tokenize":
                while (tok.peek().getType() != TokenType.EOF) {
                    System.out.println(tok.next());
                }
                break;
            case "parseExpr":
                p = new Parser(tok);
                System.out.println(p.parseExpr());
                break;
            case "parseStmt":
                p = new Parser(tok);
                System.out.println(p.parseStmt());
                break;
            case "parseMethod":
                p = new Parser(tok);
                System.out.println(p.parseMethod());
                break;
            case "parseClass":
                p = new Parser(tok);
                System.out.println(p.parseClass());
                break;
            case "parse":
                String filePath = args[1];
                try {
                    tok = new Tokenizer(Files.readString(Path.of(filePath), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    System.out.println("Failed to locate file with path "+filePath);
                    System.exit(1);
                }
                p = new Parser(tok);
                System.out.println(p.parse());
                break;
            default:
                System.err.println("Unsupported subcommand: "+args[0]);
        }
    }
}
