package com.jd.m.netcache;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 远程请求实体内容
 *
 * @author zhulx
 */
public abstract class NetEntity {

    private static final Log log = LogFactory.getLog(NetEntity.class);

    /**
     * 请求等待器
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * 清除缓存的结果集
     */
    private final Semaphore semaphore = new Semaphore(1);

    private Serializable result;

    private long awaitTimeout = 5L;

    private long keepAlive = 5000L;

    private NetInvoker invoker;

    /**
     * 获取请求的数据，在请求执行完毕之前会一直阻塞，直到请求返回，在所有等待请求返回的线程都获得了请求结果之后，
     * 等待keepAlive时间之后，由其中的一个线程删除缓存的结果map中该请求的相关信息
     *
     * @return 请求返回内容
     */
    public Serializable get() {
        try {
            latch.await(awaitTimeout, TimeUnit.SECONDS);
            if (log.isInfoEnabled()) {
                log.info(Thread.currentThread().getName() + ": NetEntity get");
            }
            if (semaphore.tryAcquire()) {
                invoker.getPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (log.isInfoEnabled()) {
                            log.info(Thread.currentThread().getName() + ": NetEntity await");
                        }
                        try {
                            Thread.sleep(keepAlive);
                        } catch (Exception e) {
                            log.error("NetEntity wait error!", e);
                        }

                        invoker.getCachedMap().remove(NetEntity.this);
                    }
                });
            }
            if (log.isInfoEnabled()) {
                log.info(Thread.currentThread().getName() + ": NetEntity return");
            }
            return this.result;
        } catch (InterruptedException e) {
            log.error("NetEntity get error!", e);
        }

        return null;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setResult(Serializable result) {
        this.result = result;
    }

    public void setInvoker(NetInvoker invoker) {
        this.invoker = invoker;
    }

    public void setAwaitTimeout(long awaitTimeout) {
        this.awaitTimeout = awaitTimeout;
    }

    public void setKeepAlive(long keepAlive) {
        this.keepAlive = keepAlive;
    }
}
