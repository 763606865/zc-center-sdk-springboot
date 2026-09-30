package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证 Ticket。
 */
public class AuthApi extends AbstractApi {

    public AuthApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse issueTicket(String uuid, String targetAppCode) {
        return issueTicket(uuid, targetAppCode, null);
    }

    /**
     * @param extra 可选扩展参数（身份、地区等），原样透传到兑换结果；传 null 表示不携带
     */
    public SapiResponse issueTicket(String uuid, String targetAppCode, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("uuid", uuid);
        payload.put("target_app_code", targetAppCode);
        if (extra != null) {
            payload.put("extra", extra);
        }
        return post("/sapi/auth/ticket", payload);
    }

    public SapiResponse exchangeTicket(String ticket) {
        return post("/sapi/auth/ticket/exchange", Map.of("ticket", ticket));
    }
}
