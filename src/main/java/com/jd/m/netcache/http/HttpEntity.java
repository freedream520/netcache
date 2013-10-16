package com.jd.m.netcache.http;

import com.jd.m.netcache.NetEntity;

/**
 * http请求实体内容
 *
 * @author zhulx
 */
public class HttpEntity extends NetEntity {
    /**
     * 请求方法：GET,POST...等等
     */
    private String method;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求参数内容
     */
    private String content;

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * 17 + method.hashCode()) + url.hashCode()) + content.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof HttpEntity) {
            HttpEntity entity = (HttpEntity) obj;
            return entity.getMethod().equals(method) &&
                    entity.getUrl().equals(url) &&
                    entity.getContent().equals(content);
        }
        return false;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
