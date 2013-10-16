package com.jd.m.netcache.http;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * http连接，实现了连接池
 *
 * @author zhulx
 */
public class HttpClient {

    private static final Log log = LogFactory.getLog(HttpClient.class);
    /**
     * http请求的警告时间
     */
    private static final int WARNING_TIME = 80;

    private DefaultHttpClient client;

    private int maxTotal;

    private int maxPerRoute;

    private int connectionTimeout;

    private int soTimeout;

    private int keepAliveTime;

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * 初始化方法
     */
    public void init() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        // http连接池
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(registry);

        // 设置所有最大的连接数为100
        connectionManager.setMaxTotal(maxTotal);
        // 设置每个路由最大连接数为20
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        // 连接超时时间
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);

        client = new DefaultHttpClient(connectionManager, params);
        // 当响应头没有Keep-Alive属性时，指定默认的连接池空闲时间
        final int keepAliveTimeMills = keepAliveTime * 1000;

        client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                if (response == null) {
                    throw new IllegalArgumentException("HTTP response may not be null");
                }
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                            log.error("httpClient keepAlive is not a number!");
                        }
                    }
                }
                return keepAliveTimeMills;
            }
        });
    }

    /**
     * http的post请求
     *
     * @param uri
     *         请求地址
     * @param content
     *         请求内容
     * @return 响应内容
     */
    public String post(String uri, String content) {
        System.out.println("post start ");
        long start = System.currentTimeMillis();
        String result = null;
        try {
            HttpPost method = new HttpPost(uri);
            if (content != null) {
                BasicHttpEntity requestEntity = new BasicHttpEntity();
                requestEntity.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
                method.setEntity(requestEntity);
            }

            HttpResponse response = client.execute(method);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error("post请求失败！请求地址为：" + uri, e);
        }

        long duration = System.currentTimeMillis() - start;
        if (duration > WARNING_TIME) {
            log.error("http post execute time: " + duration + ", request address is: " + uri);
        }

        return result;
    }

    /**
     * http的post请求
     *
     * @param uri
     *         请求地址
     * @param content
     *         请求内容
     * @return 响应内容
     */
    public String post2(String uri, String content) {
        System.out.println("post start ");
        long start = System.currentTimeMillis();
        String result = null;
        try {
            HttpPost method = new HttpPost(uri);
            if (content != null) {
                StringEntity requestEntity = new StringEntity(content, HTTP.UTF_8);
                // 设置类型
                requestEntity.setContentType("application/x-www-form-urlencoded");
                method.setEntity(requestEntity);
            }

            HttpResponse response = client.execute(method);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error("post请求失败！请求地址为：" + uri, e);
        }

        long duration = System.currentTimeMillis() - start;
        if (duration > WARNING_TIME) {
            log.error("http post execute time: " + duration + ", request address is: " + uri);
        }

        return result;
    }

    /**
     * get方法
     *
     * @param url
     *         携带参数
     * @return 请求结果
     */
    public String get(String url, String respDecode) {
        System.out.println("get start...");
        long start = System.currentTimeMillis();
        String result = null;
        if (null == respDecode) {
            respDecode = "UTF-8";
        }
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity(), respDecode);
            }
        } catch (Exception e) {
            log.error("get请求失败！请求地址为：" + url, e);
        }

        long duration = System.currentTimeMillis() - start;
        if (duration > WARNING_TIME) {
            log.error("http get execute time: " + duration + ", request address is: " + url);
        }

        return result;
    }
}
