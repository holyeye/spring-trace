package spring.trace.testweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.trace.testweb.repository.HelloRepository;

/**
 * @author: holyeye
 */

@Service
public class HelloService {

    @Autowired HelloRepository helloRepository;

    public String hello(String name) {
        helloRepository.helloQuery();
        return "hello " + name;
    }

    public String helloException() throws Exception {
        throw new Exception("강제 예외");
    }
}
