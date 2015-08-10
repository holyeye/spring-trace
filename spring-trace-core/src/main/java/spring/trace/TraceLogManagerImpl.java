package spring.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author holyeye
 */
public class TraceLogManagerImpl implements TraceLogManager {

    public static final String TRACE = "TRACE";
    public static final String APP_ERROR = "APP_ERROR";
    public static final String USER_ERROR = "USER_ERROR";
    public static final String SLOW_LOGIC = "SLOW_LOGIC";

    private static final Logger log = LoggerFactory.getLogger(TraceLogManagerImpl.class);
    private static final Logger traceLog = LoggerFactory.getLogger(TRACE);
    private static final Logger appErrorLog = LoggerFactory.getLogger(APP_ERROR);
    private static final Logger userErrorLog = LoggerFactory.getLogger(USER_ERROR);
    private static final Logger slowLog = LoggerFactory.getLogger(SLOW_LOGIC);

    private long slowTime = 1000;

    @Override
    public void setSlowTime(long slowTime) {
        log.info("slowTime = {}ms", slowTime);
        this.slowTime = slowTime;
    }

    @Override
    public void writeStartLog(String message) {

        TraceLogInfoThreadLocalManager.addDepth();
        if (TraceLogInfoThreadLocalManager.isFirstDepth()) {
            startTime();
        }

        String depthMessage = addStartSpace() + message;
        traceLog.trace(depthMessage);
        TraceLogInfoThreadLocalManager.addLog(depthMessage);
    }

    @Override
    public void writeEndLog(String message) {

        String depthMessage = addEndSpace() + message;
        traceLog.trace(depthMessage);
        TraceLogInfoThreadLocalManager.addLog(depthMessage);

        if (TraceLogInfoThreadLocalManager.isFirstDepth()) {
            profile();
            clear();
        } else {
            TraceLogInfoThreadLocalManager.removeDepth();
        }
    }

    /**
     * 마지막에 발생한 예외를 최종 예외로 인정한다. 웹 애플리케이션은 스프링MVC에서 예외를 처리해버릴 수 있기 때문에 컨트롤러에서 발생한 예외를 최종 예외로 인정해야 한다.
     *
     */
    @Override
    public void writeExceptionLog(String message, Throwable ex) {

        String depthMessage = addExceptionSpace() + message;
        traceLog.trace(depthMessage);
        TraceLogInfoThreadLocalManager.addLog(depthMessage);

        if (TraceLogInfoThreadLocalManager.isFirstDepth()) {
            setException(ex);
            profile();
            clear();
        } else {
            TraceLogInfoThreadLocalManager.removeDepth();
        }
    }

    private void clear() {
        TraceLogInfoThreadLocalManager.clear();
    }

    private void profile() {

        long responseTime = getResponseTime();

        //슬로우 로그
        if (responseTime >= slowTime) {
            String result = buildTrace();
            slowLog.error(result);
        }

        if (getException() == null) return;

        //사용자 예외
        if (getErrorLogType() == ErrorLogType.USER_ERROR) {
            String result = buildTraceAndExceptionLog();
            userErrorLog.info(result);

        } else if (getErrorLogType() == ErrorLogType.APP_ERROR) {
            //애플리케이션 예외
            String result = buildTraceAndExceptionLog();
            appErrorLog.error(result);
        }

    }

    private String buildTrace() {
        StringBuilder sb = new StringBuilder();
        buildTraceLog(sb);
        return sb.toString();
    }

    private String buildTraceAndExceptionLog() {
        StringBuilder sb = new StringBuilder();
        buildTraceLog(sb);
        if (getException() != null) {
//            sb.append("|\n");
            buildExceptionLog(sb);
        }
        return sb.toString();
    }

    private void buildTraceLog(StringBuilder sb) {

        sb.append("TRACE LOG").append("\n");

        List<String> logs = TraceLogInfoThreadLocalManager.getLogs();
        for (int i = 0; i < logs.size(); i++) {
            String s = logs.get(i);
            sb.append(s);
            if (i < logs.size() - 1) {
                sb.append("\n");
            }
        }
    }

    private void buildExceptionLog(StringBuilder sb) {
        if (getException() != null) {
            sb.append("[EXCEPTION] ").append(getExceptionTrace());
        }
    }

    protected String addStartSpace() {
        return addSpace("-->");
    }
    protected String addEndSpace() {
        return addSpace("<--");
    }
    protected String addExceptionSpace() {
        return addSpace("<X-");
    }

    private String addSpace(String prefix) {
        Integer depth = TraceLogInfoThreadLocalManager.getDepth();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            sb.append(i == depth - 1 ? "|"+prefix : "|   ");
        }
        return sb.toString();
    }

    @Override
    public void setException(Throwable ex) {
        TraceLogInfoThreadLocalManager.setException(ex);
    }

    @Override
    public Throwable getException() {
        return TraceLogInfoThreadLocalManager.getException();
    }

    private void startTime() {
        TraceLogInfoThreadLocalManager.startTime();
    }

    @Override
    public long getResponseTime() {
        return System.currentTimeMillis() - TraceLogInfoThreadLocalManager.getTime();
    }

    @Override
    public void setErrorLogType(ErrorLogType type) {
        TraceLogInfoThreadLocalManager.setErrorLogType(type);

    }

    @Override
    public ErrorLogType getErrorLogType() {
        return TraceLogInfoThreadLocalManager.getErrorLogType();
    }

    private String getExceptionTrace() {

        Throwable e = getException();
        if (e == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        if (e.getMessage() != null) {
            sw.write(e.getMessage());
        }
        sw.write("; trace=");
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
