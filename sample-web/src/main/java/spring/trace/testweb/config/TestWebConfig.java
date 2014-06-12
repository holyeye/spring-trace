package spring.trace.testweb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import spring.trace.configuration.EnableTrace;

/**
 * User: HolyEyE
 */

@Configuration
@EnableWebMvc
@EnableTrace(basePackages = "spring.trace.testweb")
@ComponentScan(basePackages = "spring.trace.testweb.controller")
public class TestWebConfig extends WebMvcConfigurerAdapter {

}
