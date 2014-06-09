package spring.trace;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: holyeye
 */
public class SpringTraceAopInterceptor extends CustomizableTraceInterceptor {

    public static final String ENTER_MESSAGE = "[$[targetType]] $[targetClassShortName].$[methodName]($[arguments])";
    public static final String EXIT_MESSAGE = ENTER_MESSAGE + " [$[returnValueCustom]] $[invocationTime]ms.";
    public static final String EXCEPTION_MESSAGE = ENTER_MESSAGE + " Exception! $[exception] $[invocationTime]ms.";

    private TraceLogManager traceLogManager;

    private String enterMessage = ENTER_MESSAGE;
    private String exitMessage = EXIT_MESSAGE;
    private String exceptionMessage = EXCEPTION_MESSAGE;

    private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+\\]");

    public static final String PLACEHOLDER_RETURN_VALUE_CUSTOM = "$[returnValueCustom]";
    public static final String PLACEHOLDER_TARGET_TYPE = "$[targetType]";

    public SpringTraceAopInterceptor(TraceLogManager traceLogManager) {
        this.traceLogManager = traceLogManager;
    }

    public void setEnterMessage(String enterMessage) {
        this.enterMessage = enterMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    protected String replacePlaceholders(String message, MethodInvocation methodInvocation,
                                         Object returnValue, Throwable throwable, long invocationTime) {

        Matcher matcher = PATTERN.matcher(message);

        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            if (PLACEHOLDER_METHOD_NAME.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(methodInvocation.getMethod().getName()));
            }
            //추가기능
            else if (PLACEHOLDER_TARGET_TYPE.equals(match)) {
                String targetType = getTargetType(methodInvocation);
                matcher.appendReplacement(output, Matcher.quoteReplacement(targetType));
            }
            else if (PLACEHOLDER_TARGET_CLASS_NAME.equals(match)) {
                String className = getClassForLogging(methodInvocation.getThis()).getName();
                matcher.appendReplacement(output, Matcher.quoteReplacement(className));
            }
            else if (PLACEHOLDER_TARGET_CLASS_SHORT_NAME.equals(match)) {
                String shortName = ClassUtils.getShortName(getClassForLogging(methodInvocation.getThis()));
                matcher.appendReplacement(output, Matcher.quoteReplacement(shortName));
            }
            else if (PLACEHOLDER_ARGUMENTS.equals(match)) {
                matcher.appendReplacement(output,
                        Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments())));
            }
            else if (PLACEHOLDER_ARGUMENT_TYPES.equals(match)) {
                appendArgumentTypes(methodInvocation, matcher, output);
            }
            //추가 기능
            else if (PLACEHOLDER_RETURN_VALUE_CUSTOM.equals(match)) {
                appendReturnValueCustom(methodInvocation, matcher, output, returnValue);
            }
            else if (PLACEHOLDER_RETURN_VALUE.equals(match)) {
                appendReturnValue(methodInvocation, matcher, output, returnValue);
            }
            else if (throwable != null && PLACEHOLDER_EXCEPTION.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(throwable.toString()));
            }
            else if (PLACEHOLDER_INVOCATION_TIME.equals(match)) {
                matcher.appendReplacement(output, Long.toString(invocationTime));
            }
            else {
                // Should not happen since placeholders are checked earlier.
                throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
            }
        }
        matcher.appendTail(output);

        return output.toString();
    }

    private String getTargetType(MethodInvocation methodInvocation) {

        Class<?> targetType = methodInvocation.getThis().getClass();

        if (hasAnnotation(targetType, Controller.class)) {
            return "Controller";
        } else if (hasAnnotation(targetType, Service.class)) {
            return "Service";
        } else if (hasAnnotation(targetType, Repository.class)) {
            return "Repository";
        } else {
            return "Trace";
        }
    }

    private boolean hasAnnotation(Class<?> targetType, Class annotationType) {
        return AnnotationUtils.findAnnotation(targetType, annotationType) != null;
    }

    private void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output) {
        Class<?>[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
        String[] argumentTypeShortNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypeShortNames.length; i++) {
            argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
        }
        matcher.appendReplacement(output,
                Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames)));
    }

    private void appendReturnValue(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {

        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        }
        else if (returnValue == null) {
            matcher.appendReplacement(output, "null");
        }
        else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(returnValue.toString()));
        }
    }


    private void appendReturnValueCustom(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {

        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        } else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(buildReturnValue(returnValue)));
        }
    }

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        return ret;
    }

    /**
     * Collection, Array, Map : size=X
     * String, Java BasicType = ALL
     * null = null
     * Object = class SimpleName
     */
    String buildReturnValue(Object returnValue) {

        if (returnValue == null) {
            return "null";
        }

        if (Collection.class.isAssignableFrom(returnValue.getClass())) {
            return "size=" + ((Collection) returnValue).size();
        } else if (Map.class.isAssignableFrom(returnValue.getClass())) {
            return "size=" + ((Map) returnValue).size();
        } else if (returnValue.getClass().isArray()) {
            return "size=" + Array.getLength(returnValue);
        } else if (isWrapperType(returnValue.getClass())) {
            return returnValue.toString();
        } else if (String.class.isAssignableFrom(returnValue.getClass())) {
            return (String) returnValue;
        } else {
            //Object
            return "<"+returnValue.getClass().getSimpleName().toString()+">";
        }
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {

        String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
        StopWatch stopWatch = new StopWatch(name);
        Object returnValue = null;
        boolean exitThroughException = false;
        try {

            stopWatch.start(name);
            writeStartLog(replacePlaceholders(enterMessage, invocation, null, null, -1));
            returnValue = invocation.proceed();
            return returnValue;
        } catch (Throwable ex) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            exitThroughException = true;

            //만약 컨트롤러면 예외를 강제로 지정한다. 컨트롤러에서 발생한 예외가 필터까지 오지 못하는 상황을 방지한다.
            if (hasAnnotation(invocation.getThis().getClass(), Controller.class)) {
                traceLogManager.setException(ex);
            }
            writeExceptionLog(replacePlaceholders(exceptionMessage, invocation, null, ex, stopWatch.getTotalTimeMillis()), ex);

            throw ex;
        } finally {
            if (!exitThroughException) {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
                writeEndLog(replacePlaceholders(exitMessage, invocation, returnValue, null, stopWatch.getTotalTimeMillis()));
            }
        }
    }


    @Override
    protected boolean isLogEnabled(Log logger) {
        return logger.isInfoEnabled();
    }

    private void writeStartLog(String message) {
        traceLogManager.writeStartLog(message);
    }

    private void writeExceptionLog(String message, Throwable ex) {
        traceLogManager.writeExceptionLog(message, ex);
    }

    private void writeEndLog(String message) {
        traceLogManager.writeEndLog(message);
    }

}
