package spring.trace.configuration;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: holyeye
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(TraceRegistrar.class)
public @interface EnableTrace {

    String[] basePackages();
}
