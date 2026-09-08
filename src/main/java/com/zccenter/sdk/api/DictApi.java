package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 数据字典增量拉取。
 *
 * @see docs/sapi/字典.md
 */
public class DictApi extends AbstractApi {

    public DictApi(ZcCenterClient client) {
        super(client);
    }

    /**
     * 通用字典类型列表。
     */
    public SapiResponse types() {
        return types(Map.of());
    }

    /**
     * 通用字典类型列表。
     */
    public SapiResponse types(Map<String, Object> payload) {
        return post("/sapi/dict/types", payload);
    }

    /**
     * 通用字典项增量拉取。
     */
    public SapiResponse items(Map<String, Object> payload) {
        return post("/sapi/dict/items", payload);
    }

    /**
     * 主数据增量拉取（area/industry/occupation/major）。
     */
    public SapiResponse master(Map<String, Object> payload) {
        return post("/sapi/dict/master", payload);
    }
}
