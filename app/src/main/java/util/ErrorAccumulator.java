package util;

import java.util.ArrayList;

public class ErrorAccumulator {
    private static ArrayList<String> errors = new ArrayList<>();

    public static void addError(String error) {
        errors.add(error);
    }

    public static void emitErrors() {
        for(String error : errors) {
            System.out.println(error);
        }
        if(errors.size() > 0) {
            System.out.println(errors.size()+" ERRORS EMITTED. ABORTING COMPILATION.");
            System.exit(1);
        }
    }
}
