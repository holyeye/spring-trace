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
import org.springframework.util.ClassUtils;
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

    /**
     * Trace 어노테이션이 설정된 config 클래스 명
     */
    protected String configClassName;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
    	this.configClassName = importMetadata.getClassName();
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
	        List<String> basePackages = findBasePackages();
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
	 * BasePackage를 찾는다.
	 *
	 * 만약에 basePackages 정보가 존재 하지 않으면
	 * 기존 EnableTrace 어노테이션이 설정 되어 있는 Config 클래스의 패키지가 기본으로 설정 된다.
	 *
	 * @return
	 */
	private List<String> findBasePackages() {
		List<String> basePackages = new ArrayList<String>();
		for (String pkg : this.annotationAttributes.getStringArray("value")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : this.annotationAttributes.getStringArray("basePackages")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : this.annotationAttributes.getClassArray("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}
		if (basePackages.isEmpty()) {
			String defaultPackageName = ClassUtils.getPackageName(configClassName);
			basePackages.add(defaultPackageName);
			log.debug("Default package name : {}", defaultPackageName);
		}
		return basePackages;
	}

	/**
	 * basePackage 배열을 기반으로 pointcut expression을 생
	 * @param basePackages
	 * @return
	 */
	private String makeExpression(List<String> basePackages) {
		Assert.notNull(basePackages);
		StringBuilder sb = new StringBuilder(); {
			sb.append("(");
			int cnt = 0;
			for (String basePackage : basePackages) {
				if(!StringUtils.hasText(basePackage)) {
					continue;
				}
				cnt++;
				sb.append(String.format("execution(* %s..*.*(..))", basePackage));
				if (basePackages.size() != cnt) {
					sb.append(" or ");
				} else {
					sb.append(") ");
				}
			}
		}
		return sb.toString();
	}
}
