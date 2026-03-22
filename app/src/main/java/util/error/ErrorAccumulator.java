package util.error;

import java.util.ArrayList;

public class ErrorAccumulator {
    private static ArrayList<Error> errors = new ArrayList<>();

    public static void addError(Error error) {
        errors.add(error);
    }

    public static void emitErrors() {
        for(Error error : errors) {
            System.out.println(error);
        }
        if(errors.size() > 0) {
            System.out.println(errors.size()+" ERRORS EMITTED. ABORTING COMPILATION.");
            System.exit(1);
        }
    }
}
