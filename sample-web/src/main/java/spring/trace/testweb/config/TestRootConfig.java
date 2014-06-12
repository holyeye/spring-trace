package spring.trace.testweb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;

/**
 * @author: holyeye
 */

@Configuration
@EnableAsync
@ComponentScan(basePackages = "spring.trace.testweb", excludeFilters = @ComponentScan.Filter(Controller.class))
public class TestRootConfig {
}
