package spring.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;

import java.util.List;


/**
 * @author: holyeye
 */
public class TraceLogInfoThreadLocalManager {

    private static Logger log = LoggerFactory.getLogger(TraceLogInfoThreadLocalManager.class);

    private static ThreadLocal<TraceLogInfo> traceLogInfoThreadLocal = new NamedThreadLocal<TraceLogInfo>("Trace Log Info ThreadLocal");
    private static ErrorLogType errorLogType;


    public static void addLog(String message) {
        getTraceLogInfo().getLogs().add(message);
    }

    public static List<String> getLogs() {
        return getTraceLogInfo().getLogs();
    }

    public static TraceLogInfo getTraceLogInfo() {
        initIfNull();
        return traceLogInfoThreadLocal.get();
    }

    public static void bindTraceLogInfo(TraceLogInfo traceLogInfo) {
        traceLogInfoThreadLocal.set(traceLogInfo);
    }

    private static void initIfNull() {
        if (traceLogInfoThreadLocal.get() == null) {
            traceLogInfoThreadLocal.set(new TraceLogInfo());
        }
    }

    public static void clear() {
        log.trace("clear");
        traceLogInfoThreadLocal.remove();
    }

    public static Throwable getException() {
        return getTraceLogInfo().getException();
    }

    public static void setException(Throwable exception) {
        getTraceLogInfo().setException(exception);
    }

    public static void startTime() {
        getTraceLogInfo().setTime(System.currentTimeMillis());
    }

    public static long getTime() {
        return getTraceLogInfo().getTime();
    }
    public static void addDepth() {
        getTraceLogInfo().addDepth();
    }
    public static boolean isFirstDepth() {
        return getDepth() == 1 ? true : false;
    }
    public static int getDepth() {
        return getTraceLogInfo().getDepth();
    }
    public static void removeDepth() {
        getTraceLogInfo().removeDepth();
    }

    public static void setErrorLogType(ErrorLogType errorLogType) {
        getTraceLogInfo().setErrorLogType(errorLogType);
    }

    public static ErrorLogType getErrorLogType() {
        return getTraceLogInfo().getErrorLogType();
    }
}
