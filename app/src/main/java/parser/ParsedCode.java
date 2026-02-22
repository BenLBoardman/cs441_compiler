package parser;

import java.lang.StringBuilder;
import java.util.ArrayList;

public class ParsedCode {
    public final Method main;
    public final ArrayList<ParserClass> classes;

    public ParsedCode(Method main, ArrayList<ParserClass> classes) {
        this.main = main;
        this.classes = classes;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`````````````````````\n\tPARSED CODE\n");
        for (ParserClass c : classes) {
            sb.append("CLASS\n");
            sb.append(c.toString() + "\n");
        }
        sb.append("MAIN\n");
        sb.append(main.toString());
        return sb.toString();
    }
}
