package com.jd.m.netcache.http;

import java.net.URLEncoder;

import com.jd.m.netcache.NetEntity;
import com.jd.m.netcache.NetInvoker;

/**
 * http请求处理
 *
 * @author zhulx
 */
public class HttpInvoker extends NetInvoker {

    private HttpClient client;

    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";

    /**
     * http请求的具体方法，执行不同的http方法
     *
     * @param entity
     *         请求实体内容
     * @return 请求返回内容
     */
    public String invoke(NetEntity entity) {
        HttpEntity httpEntity = (HttpEntity) entity;
        String method = httpEntity.getMethod().toUpperCase();
        String content = httpEntity.getContent();
        if (HTTP_POST.equals(method)) {
            return client.post2(httpEntity.getUrl(), content);
        } else if (HTTP_GET.equals(method)) {
            String url = buildUrl(httpEntity.getUrl(), content);
            return client.get(url, "UTF-8");
        }

        return null;
    }

    private String buildUrl(String url, String content) {
        StringBuilder result = new StringBuilder(url);
        if (url.contains("?")) {
            if (url.endsWith("?")) {
                result.append(content);
            } else {
                result.append("&").append(content);
            }
        } else {
            result.append("?").append(content);
        }

        return result.toString();
    }

    public void setClient(HttpClient client) {
        this.client = client;
    }
}
