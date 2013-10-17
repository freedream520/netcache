package com.jd.m.netcache.http;

import com.jd.m.netcache.NetEntity;

/**
 * http请求实体内容
 *
 * @author zhulx
 */
public class HttpEntity extends NetEntity {
    /**
     * 请求参数类型
     */
    public static enum ParamType {
        STREAM, FORM
    }

    /**
     * 请求方法：GET,POST...等等
     */
    private String method = "POST";

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求参数内容
     */
    private String content;

    /**
     * post请求时，请求参数的类型
     */
    private ParamType paramType = ParamType.FORM;

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

    /**
     * 生成一个HttpEntity对象
     *
     * @param method
     *         请求方法：GET, POST
     * @param url
     *         请求地址
     * @param content
     *         请求内容
     * @param paramType
     *         请求参数类型
     * @return HttpEntity
     */
    public static HttpEntity parse(String method, String url, String content, ParamType paramType) {
        HttpEntity entity = new HttpEntity();
        entity.setMethod(method);
        entity.setUrl(url);
        entity.setContent(content);
        entity.setParamType(paramType);
        return entity;
    }

    /**
     * 生成一个HttpEntity对象，默认方法为POST
     *
     * @param url
     *         请求地址
     * @param content
     *         请求内容
     * @param paramType
     *         请求参数类型
     * @return HttpEntity
     */
    public static HttpEntity parse(String url, String content, ParamType paramType) {
        HttpEntity entity = new HttpEntity();
        entity.setUrl(url);
        entity.setContent(content);
        entity.setParamType(paramType);
        return entity;
    }

    /**
     * 生成一个HttpEntity对象，默认请求参数为ParamType.FORM
     *
     * @param method
     *         请求方法：GET, POST
     * @param url
     *         请求地址
     * @param content
     *         请求内容
     * @return HttpEntity
     */
    public static HttpEntity parse(String method, String url, String content) {
        HttpEntity entity = new HttpEntity();
        entity.setMethod(method);
        entity.setUrl(url);
        entity.setContent(content);
        return entity;
    }

    /**
     * 生成一个HttpEntity对象，默认方法为POST，默认请求参数为ParamType.FORM
     *
     * @param url
     *         请求地址
     * @param content
     *         请求内容
     * @return HttpEntity
     */
    public static HttpEntity parse(String url, String content) {
        HttpEntity entity = new HttpEntity();
        entity.setUrl(url);
        entity.setContent(content);
        return entity;
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

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }
}
