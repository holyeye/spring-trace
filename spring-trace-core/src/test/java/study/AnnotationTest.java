package study;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author: holyeye
 */
public class AnnotationTest {

    @Test
    public void test() throws Exception {

        Service hellos = AnnotationUtils.findAnnotation(AnMock.class, Service.class);
        Service hello = AnnotationUtils.findAnnotation(AnMock.class.getDeclaredMethod("hello"), Service.class);

        Method[] declaredMethods = AnMock.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            System.out.println("declaredMethod = " + declaredMethod);
        }

        System.out.println("hellos = " + hellos);
        System.out.println("hello = " + hello);
    }

    @Test
    public void component() {
        Component annotation = AnnotationUtils.findAnnotation(AnMock.class, Component.class);
        Assert.assertNotNull(annotation);
    }

    @Service
    public static class AnMock {

        public void hello() {
        }

    }
}
