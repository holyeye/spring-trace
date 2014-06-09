package spring.trace;

/**
 * @author: holyeye
 */
public interface TraceLogManager {

    void writeStartLog(String message);

    void writeEndLog(String message);

    void writeExceptionLog(String message, Throwable ex);

    void setTimeoutMillisecond(long timeoutMillisecond);

    void setException(Throwable ex);

    Throwable getException();

    long getResponseTime();

}
