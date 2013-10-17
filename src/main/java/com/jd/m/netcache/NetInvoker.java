package com.jd.m.netcache;

import java.io.Serializable;
import java.util.concurrent.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 执行远程请求抽象类
 *
 * @author zhulx
 */
public abstract class NetInvoker<T extends NetEntity> implements Runnable {

    private static final Log log = LogFactory.getLog(NetInvoker.class);

    /**
     * 缓存结果集
     */
    private ConcurrentHashMap<T, T> cachedMap = new ConcurrentHashMap<T, T>();

    /**
     * 请求执行队列
     */
    private BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    /**
     * 请求的线程池
     */
    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1,
            new ThreadFactory() {
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
    public T addTask(T task) {
        // 如果任务的配置不正确，则抛出异常
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task configuration is not correct!");
        }

        // 将任务存入缓存的map
        T entity = cachedMap.putIfAbsent(task, task);
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
                // 从队列中取出一个任务
                T task = queue.take();
                // 设置返回结果
                task.setResult(invoke(task));
                // 通知等待线程
                task.getLatch().countDown();
            } catch (InterruptedException e) {
                // 线程被中断重新处理
                log.error("NetInvoker Thread is interrupted", e);
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
    public abstract Serializable invoke(T entity);

    /**
     * 校验要加入队列的任务是否配置正确，由子类来实现
     *
     * @param entity
     *         要加入队列的任务
     * @return true-校验正确，false-校验失败
     */
    public abstract boolean checkTask(T entity);

    public ConcurrentHashMap<T, T> getCachedMap() {
        return cachedMap;
    }

    /**
     * 在创建完对象，并且赋值完属性后调用，用在spring的init-method中
     */
    public void init() {
        pool.execute(this);
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        this.pool = pool;
    }
}
