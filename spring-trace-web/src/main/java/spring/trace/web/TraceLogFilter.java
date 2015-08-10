package spring.trace.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;
import spring.trace.ErrorLogType;
import spring.trace.TraceLogInfo;
import spring.trace.TraceLogInfoThreadLocalManager;
import spring.trace.TraceLogManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static spring.trace.web.SpringTraceWebConstant.LOG_INFO_ATTRIBUTE_NAME;

/**
 * @author: holyeye
 */
public class TraceLogFilter extends OncePerRequestFilter {

    private static Logger log = LoggerFactory.getLogger(TraceLogFilter.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String DEFAULT_TRACE_LOG_MANAGER_NAME = "traceLogManager";
    private String traceLogManagerBeanName = DEFAULT_TRACE_LOG_MANAGER_NAME;

    private volatile TraceLogManager traceLogManager;

    private TraceAsyncRequestInterceptor interceptor = new TraceAsyncRequestInterceptor();

    public void setTraceLogManagerBeanName(String traceLogManagerBeanName) {
        this.traceLogManagerBeanName = traceLogManagerBeanName;
    }

    public String getTraceLogManagerBeanName() {
        return traceLogManagerBeanName;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        boolean isFirstRequest = !isAsyncDispatch(request);
        TraceLogManager logManager = lookupTraceLogManager(request);
        Exception exception = null;

        if (isFirstRequest) {
            request = wrapMultiReadableRequest(request);
            registerAsyncInterceptor(request);
            logManager.writeStartLog(buildRequestLog(request));

            //for Servlet3 async
            request.setAttribute(LOG_INFO_ATTRIBUTE_NAME, TraceLogInfoThreadLocalManager.getTraceLogInfo());
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            exception = e;
        } finally {

            if (isAsyncStarted(request)) return;

            //for Servlet3 async
            if (!isFirstRequest) {
                TraceLogInfoThreadLocalManager.bindTraceLogInfo((TraceLogInfo) request.getAttribute(LOG_INFO_ATTRIBUTE_NAME));
            }

            setErrorLogTypeByHttpStatus(response, logManager, exception);

            if (exception == null) {
                logManager.writeEndLog(buildResponseLog(request, response, null, logManager.getResponseTime()));
            } else {
                logManager.writeExceptionLog(buildResponseLog(request, response, exception, logManager.getResponseTime()), exception);
            }

        }
    }

    private void setErrorLogTypeByHttpStatus(HttpServletResponse response, TraceLogManager logManager, Exception exception) {

        //스프링에서 처리되지 않은 예외
        if (exception instanceof NestedServletException) {
            logManager.setErrorLogType(ErrorLogType.APP_ERROR);
            return;
        }

        //사용자 예외
        if (response.getStatus() >= 400 && response.getStatus() <= 499) {
            logManager.setErrorLogType(ErrorLogType.USER_ERROR);

        } else if (response.getStatus() >= 500 && response.getStatus() <= 599) {
            //애플리케이션 예외
            logManager.setErrorLogType(ErrorLogType.APP_ERROR);
        } else {
            logManager.setErrorLogType(ErrorLogType.NONE);
        }
    }

    private HttpServletRequest wrapMultiReadableRequest(HttpServletRequest request) throws IOException {

        if(HttpReadUtils.isReadableHttpBody(request.getMethod())) {
            request.getParameterMap();//TODO 필터를 적용하기전에 parameter를 먼저 파싱해야 한다. 아직 해결하지 못한 이슈
            return new MultiReadHttpServletRequest(request, DEFAULT_ENCODING);
        } else {
            return request;
        }
    }

    private void registerAsyncInterceptor(HttpServletRequest request) {
        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        String key = getAlreadyFilteredAttributeName();
        asyncManager.registerCallableInterceptor(key, interceptor);
        asyncManager.registerDeferredResultInterceptor(key, interceptor);
    }

    private String buildRequestLog(HttpServletRequest request) {
        String remoteHost = request.getRemoteHost();
        String method = request.getMethod();
        String requestURLWithQueryString = HttpReadUtils.getRequestURLWithQueryString(request);
        String body = HttpReadUtils.getHttpBody(request, DEFAULT_ENCODING);

        return "[REQ] host=" + remoteHost + ", method=" + method + ", url=" + requestURLWithQueryString + ", body=" + body + "";
    }

    private String buildResponseLog(HttpServletRequest request, HttpServletResponse response, Exception e, long resultTime) {
        return "[RES] host=" + request.getRemoteHost() + ", method=" + request.getMethod() + ", url=" + HttpReadUtils.getRequestURLWithQueryString(request) + ", status=" + response.getStatus() + ", time=" + resultTime + "ms, ex=" + e + "\n";
    }

    protected TraceLogManager lookupTraceLogManager(HttpServletRequest request) {
        if (traceLogManager == null) {
            traceLogManager = lookupTraceLogManager();
        }
        return traceLogManager;
    }

    protected TraceLogManager lookupTraceLogManager() {

        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        String beanName = getTraceLogManagerBeanName();
        if (StringUtils.hasLength(beanName)) {
            return wac.getBean(beanName, TraceLogManager.class);
        } else {
            throw new IllegalStateException("traceLogManager가 등록되어 있지 않습니다.");
        }
    }

}
