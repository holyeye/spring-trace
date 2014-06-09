package spring.trace.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import spring.trace.TraceLogInfo;
import spring.trace.TraceLogInfoThreadLocalManager;

import java.util.concurrent.Callable;

import static spring.trace.web.SpringTraceWebConstant.*;

/**
 * @author: holyeye
 */
public class TraceAsyncRequestInterceptor extends CallableProcessingInterceptorAdapter
        implements DeferredResultProcessingInterceptor {

    private static Logger log = LoggerFactory.getLogger(TraceAsyncRequestInterceptor.class);

    @Override
    public <T> void beforeConcurrentHandling(NativeWebRequest request, Callable<T> task) throws Exception {
        TraceLogInfoThreadLocalManager.clear();
    }

    @Override
    public <T> void preProcess(NativeWebRequest request, Callable<T> task) {
        TraceLogInfo traceLogInfo = (TraceLogInfo) request.getAttribute(LOG_INFO_ATTRIBUTE_NAME, NativeWebRequest.SCOPE_REQUEST);
        TraceLogInfoThreadLocalManager.bindTraceLogInfo(traceLogInfo);
    }

    @Override
    public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) {
        TraceLogInfoThreadLocalManager.clear();
    }

    @Override
    public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {return RESULT_NONE;}

    @Override
    public <T> void afterCompletion(NativeWebRequest request, Callable<T> task) throws Exception {}


    public <T> void beforeConcurrentHandling(NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception { }
    public <T> void preProcess(NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
        log.debug("preProcess deferredResult");
    }
    public <T> void postProcess(NativeWebRequest request, DeferredResult<T> deferredResult, Object concurrentResult) throws Exception {
        log.debug("postProcess deferredResult");
    }

    public <T> boolean handleTimeout(NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception {
        return false;
    }
    public <T> void afterCompletion(NativeWebRequest request, DeferredResult<T> deferredResult) throws Exception { }
}
