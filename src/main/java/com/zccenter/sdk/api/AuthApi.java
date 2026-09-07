package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 登录认证 Ticket。
 */
public class AuthApi extends AbstractApi {

    public AuthApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse issueTicket(String uuid, String targetAppCode) {
        return post("/sapi/auth/ticket", Map.of(
                "uuid", uuid,
                "target_app_code", targetAppCode
        ));
    }

    public SapiResponse exchangeTicket(String ticket) {
        return post("/sapi/auth/ticket/exchange", Map.of("ticket", ticket));
    }
}
