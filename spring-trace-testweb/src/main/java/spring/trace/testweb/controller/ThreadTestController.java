package spring.trace.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.trace.testweb.service.HelloService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: holyeye
 */
@RestController
public class ThreadTestController {

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    @Autowired HelloService helloService;

    @RequestMapping("thread-pool")
    public String threadPool() {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                helloService.hello("holyeye");
            }
        });

        return "result";
    }

}
