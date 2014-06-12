package spring.trace.testweb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import spring.trace.configuration.EnableTrace;

/**
 * @author: holyeye
 */

@Configuration
@EnableAsync
@EnableTrace(basePackages = "spring.trace.testweb")
@ComponentScan(basePackages = {"spring.trace.testweb.service", "spring.trace.testweb.repository"})
public class TestRootConfig {
}
