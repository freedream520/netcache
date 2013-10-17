package com.jd.m.netcache.http;

import com.jd.m.netcache.NetInvoker;

/**
 * http请求处理
 *
 * @author zhulx
 */
public class HttpInvoker extends NetInvoker<HttpEntity> {

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
    public String invoke(HttpEntity entity) {
        // 请求方法：get,post
        String method = entity.getMethod().toUpperCase();
        // 请求内容
        String content = entity.getContent();
        // 请求参数类型
        HttpEntity.ParamType paramType = entity.getParamType();

        if (HTTP_POST.equals(method) && HttpEntity.ParamType.FORM.equals(paramType)) {
            return client.postForm(entity.getUrl(), content);
        } else if (HTTP_POST.equals(method) && HttpEntity.ParamType.STREAM.equals(paramType)) {
            return client.postStream(entity.getUrl(), content);
        } else if (HTTP_GET.equals(method)) {
            String url = buildUrl(entity.getUrl(), content);
            return client.get(url, "UTF-8");
        }

        return null;
    }

    /**
     * 校验http请求的请求地址，请求参数是否为空
     *
     * @param entity
     *         请求实体内容
     * @return 校验结果
     */
    @Override
    public boolean checkTask(HttpEntity entity) {
        String url = entity.getUrl().trim();
        if (url == null || url.length() == 0) {
            return false;
        }

        String content = entity.getContent().trim();
        return !(content == null || content.length() == 0);
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
