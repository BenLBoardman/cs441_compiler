
import java.nio.file.Files;


import cfg.*;
import tokenize.Tokenizer;
import util.*;
import parser.*;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;




public class App {
    public static CtrlFlowGraph cfg;
    static boolean debug = false;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <comp> infile [-o outfile] [args...]");
            System.exit(1);
        }
        String inFilePath = args[0];
        String outFilePath = "";
        boolean ssa = true, outName = false, simple = false, vn = true, typed = true;
        if(args.length >= 3 && args[1].equals("-o")) {
            
        }

        for(int nextArg =  1; nextArg < args.length; nextArg++) {
            switch (args[nextArg]) {
                case "-noSSA":
                    ssa = false;
                    break;
                case "-simpleSSA":
                    simple = true;
                    break;
                case "-noVN":
                    vn = false;
                    break;
                case "-notype":
                    typed = false;
                    break;
                case "-o":
                    if(nextArg >= (args.length - 1))
                        throw new IllegalArgumentException("Error: received -o flag but no following arg to designate output file");
                    if(outName)
                        throw new IllegalArgumentException("Error: attempted to specify illegal second output file");
                    nextArg++;
                    outFilePath = args[nextArg];
                    outName = true;
                    break;
                case "-d":
                    debug = true;
                    break;
                default:
                    System.out.print("Command-line arg "+args[nextArg]+" not recognized");
                    System.exit(1);
            }
        }
        vn = vn && ssa;
        Output.configure(debug);
        String code = "";
        try {
            code = Files.readString(Path.of("test-code/"+inFilePath), StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.err.println("Failed to locate file "+inFilePath);
            System.exit(1);
        }
        Tokenizer tok = new Tokenizer(code);
        Parser p = new Parser(tok);
        ParsedCode pc = p.parse();
        ErrorAccumulator.emitErrors(); //emit any parser errors
        cfg = new CtrlFlowGraph();
        cfg.mkCfg(pc, typed);
        ErrorAccumulator.emitErrors(); //emit any CFG errors
        if(ssa)
            cfg.toSSA(simple);
        ErrorAccumulator.emitErrors(); //emit any SSA errors
        if(vn)
            cfg.localValueNumber();
        cfg.cleanBlocks();
        ErrorAccumulator.emitErrors(); //emit any VN errors
        if(outFilePath == "") {
            System.out.println(cfg);
            return;
        }
        try {
            Files.write(Path.of("test-out/"+outFilePath), cfg.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            System.err.println("Cannot write code to file "+outFilePath);
            e.printStackTrace();
        }
    }

    
}
