package org.apache.log4j;

public final class Logger {

    private static LogCanvas logCanvas = new LogCanvas();

    private static final long start = System.currentTimeMillis();

    static Priority priority = Priority.DEBUG;

    private String className;

    public static LogCanvas getLogCanvas() {
        return logCanvas;
    }

    private Logger(String name) {
        className = name;
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }
    
    public static Logger getLogger(Class clazz) {
        return new Logger(clazz.getName());
    }

    private void log(Priority level, Object msg, Throwable ex) {
        if (level.isGreaterOrEqual(priority)) {
            String entry = Long.toString(System.currentTimeMillis() - start) + " " + level.toString() + " "
                    + className + " - " + msg;
            //logCanvas.addEntry(entry);
            System.out.println(entry);
            if (ex != null) {
                String message = ex.getMessage();
                //logCanvas.addEntry(message);
                System.out.println(message);
            }
        }
    }

    public void debug(Object msg) {
        log(Priority.DEBUG, msg, null);
    }

    public void debug(Object msg, Throwable ex) {
        log(Priority.DEBUG, msg, ex);
    }

    public void info(Object msg) {
        log(Priority.INFO, msg, null);
    }

    public void info(Object msg, Throwable ex) {
        log(Priority.INFO, msg, ex);
    }

    public void warn(Object msg) {
        log(Priority.WARN, msg, null);
    }

    public void warn(Object msg, Throwable ex) {
        log(Priority.WARN, msg, ex);
    }

    public void error(Object msg) {
        log(Priority.ERROR, msg, null);
    }

    public void error(Object msg, Throwable ex) {
        log(Priority.ERROR, msg, ex);
    }

    public void fatal(Object msg) {
        log(Priority.FATAL, msg, null);
    }

    public void fatal(Object msg, Throwable ex) {
        log(Priority.FATAL, msg, ex);
    }

    public void setPriority(Priority p) {
        priority = p;
    }

    public boolean isInfoEnabled() {
        return Priority.INFO.isGreaterOrEqual(priority);
    }

    public boolean isDebugEnabled() {
        return Priority.DEBUG.isGreaterOrEqual(priority);
    }

}
