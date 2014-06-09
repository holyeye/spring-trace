package study;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author: holyeye
 */
public class ThreadLocalTest {

    static final ThreadLocal<String> userName = new ThreadLocal();
    static final ThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<String>();
    ThreadPoolExecutor executorService = new ThreadPoolExecutor2(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    ;


    @Test
    public void threadLocal() throws Exception {
        userName.set("Jane");
        Runnable run = new Runnable() {
            @Override
            public void run() {
                println(userName);
            }
        };
        println(userName);
        new Thread(run).start();

    }

    @Test
    public void inheritableThreadLocal() throws Exception {

        inheritableThreadLocal.set("Jane");
        Runnable run = new Runnable() {
            @Override
            public void run() {
                println(inheritableThreadLocal);
            }
        };
        new Thread(run).start();
        println(inheritableThreadLocal);
    }

    @Test
    public void pool() throws Exception {

        inheritableThreadLocal.set("hello");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                println(inheritableThreadLocal);
            }
        });
        inheritableThreadLocal.set("hi");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                println(inheritableThreadLocal);
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                println(inheritableThreadLocal);
            }
        });

        println(inheritableThreadLocal);
    }

    private void println(ThreadLocal<String> threadLocal) {
        System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
    }

    public static class ThreadPoolExecutor2 extends ThreadPoolExecutor {

        public ThreadPoolExecutor2(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            inheritableThreadLocal.remove();
            System.out.println("t = "+Thread.currentThread().getName()+"//" + t);
        }
    }
}
