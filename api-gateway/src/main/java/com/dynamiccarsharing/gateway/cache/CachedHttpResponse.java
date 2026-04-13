package com.dynamiccarsharing.gateway.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CachedHttpResponse {

    private int statusCode;
    private Map<String, List<String>> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public CachedHttpResponse() {
    }

    public CachedHttpResponse(int statusCode, Map<String, List<String>> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
