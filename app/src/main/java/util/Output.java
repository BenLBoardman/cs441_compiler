package util;

public class Output {
    private static boolean debug;

    public static void configure(boolean debug) {
        Output.debug = debug;
    }

    public static void debug(String... str) {
        if(debug)
            System.out.println(str);
    }
}
