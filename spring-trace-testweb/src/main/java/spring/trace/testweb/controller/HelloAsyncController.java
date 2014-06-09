package spring.trace.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.trace.testweb.service.HelloAsyncService;
import spring.trace.testweb.service.HelloService;

import java.util.concurrent.Callable;

/**
 * @author: holyeye
 */
@RestController
public class HelloAsyncController {

    @Autowired HelloAsyncService helloAsyncService;
    @Autowired HelloService helloService;

    @RequestMapping("hello-async")
    public String helloAsync() {
        helloAsyncService.asyncHello();
        helloAsyncService.asyncHello();
        return "hello-async";
    }

    @RequestMapping("hello-callable")
    public Callable<String> helloCallable() throws InterruptedException {
//        Thread.sleep(100);
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
//                Thread.sleep(1000);
                return helloService.hello("hi");
            }
        };
    }

    @RequestMapping("hello-callable-exception")
    public Callable<String> helloCallableException() throws InterruptedException {
//        Thread.sleep(100);
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
//                Thread.sleep(1000);
                throw new RuntimeException("강제예외");
//                return helloService.hello("hi");
            }
        };
    }

    @RequestMapping("hello-callable-exception2")
    public Callable<String> helloCallableException2()  {
        throw new RuntimeException("강제예외");
    }

}
