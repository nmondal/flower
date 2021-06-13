package flower;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
    static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS" ;
    static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial( ()-> new SimpleDateFormat(DATE_TIME_FORMAT));

    private static boolean DISABLE = false;

    public static synchronized void enable(){
        DISABLE = false;
    }

    public static synchronized void disable(){
        DISABLE = true;
    }

    public static void info(String fmt, Object... args) {
        if ( DISABLE ) return;
        final String res =  dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.out.println(res);
    }

    public static void error(Throwable t, String fmt, Object... args) {
        if ( DISABLE ) return;
        t.printStackTrace(System.err);
        final String res = dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.err.println(res);
    }

    public static void warn(String fmt, Object... args) {
        if ( DISABLE ) return;
        final String res = dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.err.println(res);
    }
}
