package com.jd.m.netcache;

import java.io.Serializable;
import java.util.concurrent.*;

/**
 * 执行远程请求抽象类
 *
 * @author zhulx
 */
public abstract class NetInvoker implements Runnable {
    /**
     * 缓存结果集
     */
    private ConcurrentHashMap<NetEntity, NetEntity> cachedMap = new ConcurrentHashMap<NetEntity, NetEntity>();

    /**
     * 请求执行队列
     */
    private BlockingQueue<NetEntity> queue = new LinkedBlockingQueue<NetEntity>();

    /**
     * 请求的线程池
     */
    private ExecutorService pool = Executors.newFixedThreadPool(10, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable task) {
            Thread t = new Thread(task);
            t.setDaemon(true);
            return t;
        }
    });

    /**
     * 添加一个请求任务到队列中，如果队列中存在相同的请求，则直接返回队列中的请求，否则将新请求加入到队列中
     *
     * @param task
     *         请求任务
     * @return 需要执行的请求任务
     */
    public NetEntity addTask(NetEntity task) {
        // 将任务存入缓存的map
        NetEntity entity = cachedMap.putIfAbsent(task, task);
        if (entity == null) {
            queue.offer(task);
            entity = task;
        }

        return entity;
    }

    /**
     * 请求执行任务，从队列中取出一个任务，执行执行，执行完成后设置返回的结果，并且通知等待结果的线程
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (!Thread.interrupted()) {
                    // 从队列中取出一个任务
                    NetEntity task = queue.take();
                    // 设置返回结果
                    task.setResult(invoke(task));
                    // 通知等待线程
                    task.getLatch().countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 执行请求的具体类，由子类来实现
     *
     * @param entity
     *         请求实体内容
     * @return 请求结果
     */
    public abstract Serializable invoke(NetEntity entity);

    public ConcurrentHashMap<NetEntity, NetEntity> getCachedMap() {
        return cachedMap;
    }

    public void after() {
        pool.execute(this);
    }

}
