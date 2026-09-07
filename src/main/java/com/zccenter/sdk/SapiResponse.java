package com.zccenter.sdk;

import java.util.Collections;
import java.util.Map;

/**
 * 统一的 SAPI 业务响应封装。
 */
public final class SapiResponse {

    private final int statusCode;
    private final Map<String, Object> payload;
    private final Map<String, String> headers;
    private final String rawBody;

    public SapiResponse(int statusCode, Map<String, Object> payload, Map<String, String> headers, String rawBody) {
        this.statusCode = statusCode;
        this.payload = payload == null ? Map.of() : Collections.unmodifiableMap(payload);
        this.headers = headers == null ? Map.of() : Collections.unmodifiableMap(headers);
        this.rawBody = rawBody == null ? "" : rawBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataAsMap() {
        Object data = payload.get("data");
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    public Object getData() {
        return payload.get("data");
    }

    public String getMessage() {
        Object msg = payload.get("msg");
        return msg == null ? "" : String.valueOf(msg);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRawBody() {
        return rawBody;
    }
}
