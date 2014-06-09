package spring.trace.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import spring.trace.SpringTraceAopInterceptor;
import spring.trace.TraceLogManager;
import spring.trace.TraceLogManagerImpl;

/**
 * @author: holyeye
 */

@Configuration
@EnableAspectJAutoProxy
public class SimpleTraceConfiguration implements ImportAware {

    private static Logger log = LoggerFactory.getLogger(SimpleTraceConfiguration.class);

    private String[] basePackages;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        AnnotationAttributes enabled = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(
                EnableTrace.class.getName(), false));

        Assert.notNull(enabled, "@EnableTrace is not present on importing class " + importMetadata.getClassName());

        basePackages = enabled.getStringArray("basePackages");
    }

    @Bean
    public TraceLogManager traceLogManager() {
        TraceLogManager traceLogManager = new TraceLogManagerImpl();
        traceLogManager.setTimeoutMillisecond(0);
        return traceLogManager;
    }

    @Bean
    public Advisor packageTraceAdvisor() {

        //TODO 여려 패키지를 받도록 수정해야 합니다.
        String basePackage = basePackages[0];

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* " + basePackage + "..*.*(..)) and !@within("+Configuration.class.getName()+") and !@within("+ ControllerAdvice.class.getName()+")");
        DefaultPointcutAdvisor pointcutAdvisor = new DefaultPointcutAdvisor(pointcut, new SpringTraceAopInterceptor(traceLogManager()));
        pointcutAdvisor.setOrder(Integer.MAX_VALUE);
        return pointcutAdvisor;
    }
}
