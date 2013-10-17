package com.jd.m.netcache.http;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 测试http接口
 *
 * @author zhulx
 */
public class HttpTest {

    private static HttpInvoker invoker = new HttpInvoker();

    private ExecutorService pool = Executors.newFixedThreadPool(10);

    @BeforeClass
    public static void init() {
        HttpClient httpClient = new HttpClient();
        httpClient.setMaxTotal(100);
        httpClient.setMaxPerRoute(20);
        httpClient.setConnectionTimeout(3000);
        httpClient.setSoTimeout(5000);
        httpClient.setKeepAliveTime(30);
        httpClient.init();
        invoker.setClient(httpClient);

        invoker.init();
    }

    /**
     * 测试get方法
     */
    @Test
    public void testGet() {
        final CountDownLatch latch = new CountDownLatch(15);
        for (int i = 0; i < 10; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    HttpEntity entity = new HttpEntity();
                    entity.setUrl("http://stuangw.m.jd.com/getTeamOrderById");
                    entity.setMethod("get");
                    entity.setContent("body=%7B%22id%22%3A%22643476916%22%7D");

                    entity.setKeepAlive(2000);

                    entity.setInvoker(invoker);
                    entity = invoker.addTask(entity);
                    System.out.println(entity.get());
                    latch.countDown();
                }
            });
        }

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    HttpEntity entity = new HttpEntity();
                    entity.setUrl("http://stuangw.m.jd.com/getTeamOrderById");
                    entity.setMethod("get");
                    entity.setContent("body=%7B%22id%22%3A%22643476916%22%7D");

                    entity.setKeepAlive(2000);

                    entity.setInvoker(invoker);
                    entity = invoker.addTask(entity);
                    System.out.println(entity.get());
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 测试post方法
     */
    @Test
    public void testPost() {
        final CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    HttpEntity entity = new HttpEntity();
                    entity.setUrl("http://stuangw.m.jd.com/getTeamOrderById");
                    entity.setMethod("post");
                    entity.setContent("body={\"id\":\"643476916\"}");

                    entity.setInvoker(invoker);
                    entity = invoker.addTask(entity);
                    System.out.println(entity.get());
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            // ignore
        }
    }
}
