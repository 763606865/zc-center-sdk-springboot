package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 生态产品自定义接口集合的基类。
 *
 * <p>子类只描述接口路径及业务参数；签名、加密、验签和异常处理统一交给 {@link ZcCenterClient}。</p>
 */
public abstract class AbstractApi {

    protected final ZcCenterClient client;

    protected AbstractApi(ZcCenterClient client) {
        this.client = client;
    }

    protected SapiResponse post(String path, Map<String, Object> payload) {
        return client.post(path, compact(payload));
    }

    protected SapiResponse get(String path, Map<String, Object> query) {
        return client.get(path, compact(query));
    }

    /**
     * 去掉 null，避免把未使用的可选参数带进请求体。
     */
    protected Map<String, Object> compact(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        params.forEach((key, value) -> {
            if (value != null) {
                out.put(key, value);
            }
        });
        return out;
    }
}
