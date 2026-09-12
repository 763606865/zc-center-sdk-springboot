package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 职位同步 SAPI。
 *
 * @see docs/sapi/职位.md
 */
public class JobApi extends AbstractApi {

    public JobApi(ZcCenterClient client) {
        super(client);
    }

    /**
     * 增量拉取可访问职位（含删除 tombstone）。
     * POST /sapi/job/list
     */
    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/job/list", params == null ? Map.of() : params);
    }

    /** POST /sapi/job/detail */
    public SapiResponse detail(String uuid) {
        return post("/sapi/job/detail", Map.of("uuid", uuid));
    }

    /**
     * 归属应用向自有职位库上报职位。
     * POST /sapi/job/report
     */
    public SapiResponse report(Map<String, Object> job) {
        return post("/sapi/job/report", job == null ? Map.of() : job);
    }

    /**
     * 更新职位（归属可改内容+状态；订阅仅 status）。
     * POST /sapi/job/update
     */
    public SapiResponse update(Map<String, Object> job) {
        return post("/sapi/job/update", job == null ? Map.of() : job);
    }
}
