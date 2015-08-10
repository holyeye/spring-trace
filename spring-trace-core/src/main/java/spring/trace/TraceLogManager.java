package spring.trace;

/**
 * @author holyeye
 */
public interface TraceLogManager {

    void writeStartLog(String message);

    void writeEndLog(String message);

    void writeExceptionLog(String message, Throwable ex);

    void setSlowTime(long slowTime);

    void setException(Throwable ex);

    Throwable getException();

    long getResponseTime();

    void setErrorLogType(ErrorLogType type);
    ErrorLogType getErrorLogType();

}
