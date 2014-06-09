# spring-trace


사용방법
```java
@EnableTrace(basePackages = "spring.trace.testweb")
```

예)

```java
@Configuration
@EnableTrace(basePackages = "spring.trace.testweb")
public class TargetWebConfig extends WebMvcConfigurerAdapter {

}
```