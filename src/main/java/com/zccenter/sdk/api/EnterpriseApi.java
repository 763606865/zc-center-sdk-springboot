package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 企业与职工。
 *
 * @see docs/sapi/企业.md
 */
public class EnterpriseApi extends AbstractApi {

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_MEMBER = 2;

    public EnterpriseApi(ZcCenterClient client) {
        super(client);
    }

    /**
     * 上报企业（按统一社会信用代码幂等）。
     */
    public SapiResponse report(Map<String, Object> payload) {
        return post("/sapi/enterprise/report", payload);
    }

    /** 按企业 UUID、信用代码或企业编码获取详情。 */
    public SapiResponse detail(Map<String, Object> payload) {
        return post("/sapi/enterprise/detail", payload);
    }

    /**
     * 加入职工。
     */
    public SapiResponse addMember(Map<String, Object> payload) {
        return post("/sapi/enterprise/member/add", payload);
    }

    /**
     * 移除职工。
     */
    public SapiResponse removeMember(Map<String, Object> payload) {
        return post("/sapi/enterprise/member/remove", payload);
    }
}
