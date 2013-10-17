## netcache

网络请求并发控制，多个相同的请求并发访问只发送一个远程请求，目前已实现http协议，待实现的有webservice, hessian或者db操作等。

### 示例

java代码：

    public class HttpCacheTest {
        public static void main(String[] args) {
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-config.xml");
            NetInvoker invoker = (NetInvoker) ctx.getBean("httpInvoker");
            ExecutorService pool = Executors.newFixedThreadPool(10);

            Task task = new Task(invoker);

            // 首先并发执行10个相同的任务，这10个请求只发送一次远程http请求
            for (int i=0; i < 10; i++) {
                pool.execute(task);
            }

            // 等待3秒，之后缓存的结果已经清除
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 并发执行5个相同请求，这5个请求只发送一次请求，但是与上面10个请求不共享请求结果
            for (int i=0; i < 5; i++) {
                pool.execute(task);
            }
        }

        private static class Task implements Runnable {
            private NetInvoker<HttpEntity> invoker;

            private Task(NetInvoker<HttpEntity> invoker) {
                this.invoker = invoker;
            }

            @Override
            public void run() {
                HttpEntity entity = HttpEntity.parse("http://stuangw.m.jd.com/getTeamCategory", "body={\"pid\":0}");
                entity.setInvoker(invoker);
                // 等待2秒后清除缓存结果
                entity.setKeepAlive(2000);
                entity = invoker.addTask(entity);
                System.out.println(entity.get());
            }
        }
    }

spring配置文件：spring-config.xml

    <bean id="httpClient" class="com.jd.m.netcache.http.HttpClient" init-method="init">
            <!-- 连接池最大连接数 -->
            <property name="maxTotal" value="100"/>
            <!-- 每个路由最大连接数 -->
            <property name="maxPerRoute" value="20"/>
            <!-- 连接超时时间,单位毫秒 -->
            <property name="connectionTimeout" value="3000"/>
            <!-- 读取超时时间,单位毫秒 -->
            <property name="soTimeout" value="5000"/>
            <!-- 服务器未设置keep-alive时间时默认的keepAlive时间，单位秒 -->
            <property name="keepAliveTime" value="30"/>
    </bean>

    <bean id="httpInvoker" class="com.jd.m.netcache.http.HttpInvoker" init-method="init">
        <property name="client" ref="httpClient" />
    </bean>