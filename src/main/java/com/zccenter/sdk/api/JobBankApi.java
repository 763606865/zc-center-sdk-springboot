package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 职位库 SAPI。
 *
 * @see docs/sapi/职位.md
 */
public class JobBankApi extends AbstractApi {

    public JobBankApi(ZcCenterClient client) {
        super(client);
    }

    /**
     * 列出当前应用可访问的已发布职位库。
     * POST /sapi/job-bank/list
     */
    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/job-bank/list", params == null ? Map.of() : params);
    }

    /**
     * 按 UUID 获取职位库详情（含已发布职位数 job_count）。
     * POST /sapi/job-bank/detail
     */
    public SapiResponse detail(String uuid) {
        return post("/sapi/job-bank/detail", Map.of("uuid", uuid));
    }
}
