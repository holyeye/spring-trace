package spring.trace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: holyeye
 */
public class TraceLogInfo {

    private List<String> logs = new ArrayList<String>();
    private Throwable exception = null;
    private Long time = System.currentTimeMillis();
    private int depth;
    private ErrorLogType errorLogType = ErrorLogType.APP_ERROR; //Default

    public List<String> getLogs() {
        return logs;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getDepth() {
        return depth;
    }
    public void addDepth() {
        depth++;
    }
    public void removeDepth() {
        depth--;
    }

    @Override
    public String toString() {
        return "TraceLogInfo{" +
                "logs=" + logs +
                ", exception=" + exception +
                ", time=" + time +
                ", depth=" + depth +
                '}';
    }

    public void setErrorLogType(ErrorLogType errorLogType) {
        this.errorLogType = errorLogType;
    }

    public ErrorLogType getErrorLogType() {
        return errorLogType;
    }
}
