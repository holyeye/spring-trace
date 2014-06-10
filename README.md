# spring-trace

## 스프링 Trace 소개

스프링 Trace를 사용하면 손쉽게 애플리케이션 호출 현황을 추적할 수 있습니다.

**적용 예시**

    [REQ] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, body={username:"hello"}
    |-->[Controller] HelloController.test()
    |   |-->[Service] HelloService.hello(holyeye)
    |   |   |-->[Repository] HelloRepository.helloQuery()
    |   |   |<--[Repository] HelloRepository.helloQuery() [size=2] 1ms.
    |   |<--[Service] HelloService.hello(holyeye) [<Member>] 1ms.
    |<--[Controller] HelloController.test() [hello holyeye] 1ms.
    [RES] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, status=200, time=3ms, ex=null
    
## 빌드 TODO

## 테스트 TODO

## 기능

**로그 출력 기능**

- 실시간 로그 출력(`TRACE`)
- 누적된 로그 출력
    - 너무 느린 로직 로그(`SLOW_LOGIC`) : 특정 시간 이상 걸린 누적된 로그를 출력합니다.
    - 예외 발생 로직 로그(`APP_ERROR`)  : 예외가 발생하면 누적된 로그를 출력합니다. 

**로그 기능**

- 메서드 호출 시간을 알 수 있습니다.
    - ex) `hello.finds() took 2ms. [size=2]`

- 메서드 호출 파라미터와 반환값을 추적할 수 있습니다.
    - 반환 값이 `null`이면 `null`을 출력합니다. ex) `[null]`
    - 반환 값이 객체면 객체 타입을 출력합니다. ex) `[<Member>]`
    - 반환 값이 컬렉션이나 배열이면 사이즈 반환합니다. ex) `[size=10]`
         
- HTTP 요청
    - 다양한 HTTP 요청 정보를 추적할 수 있습니다. 특별히 HTTP Body 정보도 출력합니다. 

## 필수 준비물과 주의사항

- 스프링 프레임워크를 사용해야 합니다.
- 현재는 어노테이션 기반의 설정만 지원합니다.
- 아직은 실험적인 단계입니다.

## 사용 방법

스프링 프레임워크에 `@EnableTrace`를 설정합니다.
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

만약 웹 애플리케이션이면 `spring.trace.web.TraceLogFilter` 필터도 추가해줍니다.

```java
public class TestWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    //...
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{new TraceLogFilter()};
    }
}
```

다음으로 `logback.xml`을 설정합니다.

```xml
<configuration>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{MM-dd HH:mm:ss} [%thread] %.-1level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>

    <!-- 실시간 TRACE -->
    <logger name="TRACE" level="trace" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>
    <!-- 애플리케이션 예외 -->
    <logger name="APP_ERROR" level="info" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>
    <!-- 느린 로직 -->
    <logger name="SLOW_LOGIC" level="info" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>
    
</configuration>
```

-----------------------

> **참고:** 운영시에 너무 많은 로그를 남기는 것은 성능상 문제가 될 수 있습나다. 운영시에는 실시간 로그는 끄고 애플리케이션 예외 로그와 느린 로직만 출력하도록 설정하는 것을 추천합니다. 

-----------------------


## 출력 결과

**실시간 로그**

    06-09 23:14:44 TRACE - [REQ] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, body=null
    06-09 23:14:44 TRACE - |-->[Controller] HelloController.test()
    06-09 23:14:44 TRACE - |   |-->[Service] HelloService.hello(holyeye)
    06-09 23:14:44 TRACE - |   |   |-->[Repository] HelloRepository.helloQuery()
    06-09 23:14:44 TRACE - |   |   |<--[Repository] HelloRepository.helloQuery() [void] 1ms.
    06-09 23:14:44 TRACE - |   |<--[Service] HelloService.hello(holyeye) [hello holyeye] 1ms.
    06-09 23:14:44 TRACE - |<--[Controller] HelloController.test() [hello holyeye] 1ms.
    06-09 23:14:44 TRACE - [RES] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, status=200, time=3ms, ex=null

**느린 로직 로그**

    06-09 23:14:44 [http-nio-8080-exec-6] E SLOW_LOGIC - TRACE LOG
    [REQ] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, body=null
    |-->[Controller] HelloController.test()
    |   |-->[Service] HelloService.hello(holyeye)
    |   |   |-->[Repository] HelloRepository.helloQuery()
    |   |   |<--[Repository] HelloRepository.helloQuery() [void] 1ms.
    |   |<--[Service] HelloService.hello(holyeye) [hello holyeye] 1ms.
    |<--[Controller] HelloController.test() [hello holyeye] 1ms.
    [RES] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/test, status=200, time=3ms, ex=null

**예외 발생 로직 로그**

    06-09 23:28:28 [http-nio-8080-exec-9] E APP_ERROR - TRACE LOG
    [REQ] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/exception, body=null
    |-->[Controller] HelloController.exception()
    |   |-->[Service] HelloService.helloException()
    |   |<X-[Service] HelloService.helloException() Exception! java.lang.Exception: 강제 예외 1ms.
    |<X-[Controller] HelloController.exception() Exception! java.lang.Exception: 강제 예외 1ms.
    [RES] host=0:0:0:0:0:0:0:1, method=GET, url=http://localhost:8080/exception, status=200, time=6ms, ex=org.springframework.web.util.NestedServletException: Request processing failed; nested exception is java.lang.Exception: 강제 예외
    [EXCEPTION] Request processing failed; nested exception is java.lang.Exception: 강제 예외; trace=org.springframework.web.util.NestedServletException: Request processing failed; nested exception is java.lang.Exception: 강제 예외
    	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:973)
    	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:852)
    	at javax.servlet.http.HttpServlet.service(HttpServlet.java:621)
    	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:837)
    	at javax.servlet.http.HttpServlet.service(HttpServlet.java:728)
    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:305)
    	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:210)
    	at spring.trace.web.TraceLogFilter.doFilterInternal(TraceLogFilter.java:74)
    	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)
    	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:243)
        ...

## 설정

TODO
- 슬로우 로직 시간 설정


## 동작 원리

지정한 패키지 하위 스프링 빈에 로그 수집용 AOP(`spring.trace.SpringTraceAopInterceptor`)를 적용하고, 
HTTP 요청에 대해서는 `spring.trace.web.TraceLogFilter`를 사용해서 추적용 로그를 생성합니다.

이 로그들은 실시간으로 출력되기도 하지만 예외가 발생하거나 너무 느린 로직이라고 판단되면 로그 이력 전체를 출력해야 합니다.
로그 이력 전체를 보관하기 위해 내부적으로 `ThreadLocal`에 로그 정보를 보관합니다. 그리고 `ThreadLocal`에 보관된 로그는 적절히 시점에 제거 됩니다.


## 참고한 기능

스프링 프레임워크의 `org.springframework.aop.interceptor.CustomizableTraceInterceptor`에서 많은 영감을 받았습니다.
이 클래스를 확장하면서 스프링 Trace 프로젝트가 시작되었습니다.  

## TODO

- 로깅 트랜잭션 ID 부여하기
- 스프링 설정 읽어서 동작하기(실행 시간 옵션 처리)
- 트랜잭션 상태 여부 적절히 로그 남기기
- @Async에서 적절히 로그 남기기
- XML 설정 기능
- Interceptor 제공하기

## License

Spring Trace is released under version 2.0 of the Apache License.