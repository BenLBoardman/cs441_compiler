package parser;

import java.lang.StringBuilder;
import java.util.ArrayList;

public class ParsedCode {
    public final ASTMethod main;
    public final ArrayList<ASTClass> classes;

    public ParsedCode(ASTMethod main, ArrayList<ASTClass> classes) {
        this.main = main;
        this.classes = classes;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`````````````````````\n\tPARSED CODE\n");
        for (ASTClass c : classes) {
            sb.append("CLASS\n");
            sb.append(c.toString() + "\n");
        }
        sb.append("MAIN\n");
        sb.append(main.toString());
        return sb.toString();
    }
}
