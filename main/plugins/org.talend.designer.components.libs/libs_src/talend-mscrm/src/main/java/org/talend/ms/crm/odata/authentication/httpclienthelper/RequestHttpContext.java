package org.talend.ms.crm.odata.authentication.httpclienthelper;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public final class RequestHttpContext {

    private String method;
    private String base;
    private Map<String, String> params;
    private Map<String, String> headers;
    private boolean followRedirects;
    private int connectionTimeout;
    private int readTimeout;
    private String bodyContent = "";

    public RequestHttpContext(String method, String base, Map<String, String> params, Map<String, String> headers, boolean followRedirects, int connectionTimeout, int readTimeout) {
        this.method = method;
        this.base = base;
        this.params = params;
        this.headers = headers;
        this.followRedirects = followRedirects;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(Map<String, String> form) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String, String> e : form.entrySet()) {
            if(!first){
                sb.append("&");
            }
            first = false;

            sb.append(e.getKey()).append("=").append(HttpClient.encodeValue(e.getValue()));
        }

        this.setBodyContent(sb.toString());
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
