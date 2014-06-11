package spring.trace.configuration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;

import spring.trace.SpringTraceAopInterceptor;
import spring.trace.TraceLogManager;
import spring.trace.TraceLogManagerImpl;

/**
 * @author: holyeye
 */

@Configuration
public class SimpleTraceConfiguration implements ImportAware {

    private static final Logger log = LoggerFactory.getLogger(SimpleTraceConfiguration.class);

    protected AnnotationAttributes annotationAttributes;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.annotationAttributes = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(
                EnableTrace.class.getName(), false));
        Assert.notNull(this.annotationAttributes, "@EnableTrace is not present on importing class " + importMetadata.getClassName());
    }

    @Bean
    public TraceLogManager traceLogManager() {
        TraceLogManager traceLogManager = new TraceLogManagerImpl();
        traceLogManager.setTimeoutMillisecond(0);
        return traceLogManager;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor packageTraceAdvisor() {
    	ComposablePointcut resultPointcut = new ComposablePointcut();
    	{
	        String[] basePackages = this.annotationAttributes.getStringArray("basePackages");
	        String pointcutExpression = makeExpression(basePackages);
	        AspectJExpressionPointcut packagePointcut = new AspectJExpressionPointcut();
	        log.debug("Include package Pointcut expression : {}", pointcutExpression);
			packagePointcut.setExpression(pointcutExpression);
	        resultPointcut.intersection((Pointcut) packagePointcut);
    	}
    	AspectJExpressionPointcut basePointcut = new AspectJExpressionPointcut();
    	basePointcut.setExpression("!@within("+Configuration.class.getName()+") and !@within("+ ControllerAdvice.class.getName()+")");
    	resultPointcut.intersection((Pointcut) basePointcut);

        DefaultPointcutAdvisor pointcutAdvisor = new DefaultPointcutAdvisor(resultPointcut, new SpringTraceAopInterceptor(traceLogManager()));
        pointcutAdvisor.setOrder(Integer.MAX_VALUE);
        return pointcutAdvisor;
    }

	/**
	 * basePackage 배열을 기반으로 pointcut expression을 생
	 * @param basePackages
	 * @return
	 */
	private String makeExpression(String[] basePackages) {
		String[] verifyBasePackages = verifyArray(basePackages);
		int basePackagesLength = verifyBasePackages.length;
		if(basePackagesLength == 0) {
			throw new IllegalArgumentException("\"basePackages\" not found");
		}
		StringBuilder sb = new StringBuilder(); {
			sb.append("(");
			int cnt = 0;
			for (String basePackage : verifyBasePackages) {
				if(!StringUtils.hasText(basePackage)) {
					continue;
				}
				cnt++;
				sb.append(String.format("execution(* %s..*.*(..))", basePackage));
				if (basePackagesLength != cnt) {
					sb.append(" or ");
				} else {
					sb.append(") ");
				}
			}
		}
		String pointcutExpression = sb.toString();
		return pointcutExpression;
	}

	/**
	 * 내부에 문자열이 없는 Array를 검사하여 제거
	 * @param array
	 * @return
	 */
	private String[] verifyArray(String[] array) {
		List<String> list = new ArrayList<String>(array.length);
		for(String e : array) {
			if(StringUtils.hasText(e)) {
				list.add(e);
			}
		}
		return list.toArray(new String[0]);
	}
}
