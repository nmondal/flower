package flower;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
    static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS" ;
    static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial( ()-> new SimpleDateFormat(DATE_TIME_FORMAT));

    public static void info(String fmt, Object... args) {
        final String res =  dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.out.println(res);
    }

    public static void error(Throwable t, String fmt, Object... args) {
        t.printStackTrace(System.err);
        final String res = dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.err.println(res);
    }

    public static void warn(String fmt, Object... args) {
        final String res = dateFormat.get().format(new Date()) + " | " + String.format(fmt, args);
        System.err.println(res);
    }
}
