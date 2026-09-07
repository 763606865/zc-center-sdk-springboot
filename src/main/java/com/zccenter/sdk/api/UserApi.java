package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 跨生态用户。
 */
public class UserApi extends AbstractApi {

    public UserApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse register(String mobile) {
        return register(mobile, "+86", null, null);
    }

    public SapiResponse register(String mobile, String countryCode, String nickname, String avatar) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mobile", mobile);
        payload.put("country_code", countryCode == null ? "+86" : countryCode);
        payload.put("nickname", nickname);
        payload.put("avatar", avatar);
        return post("/sapi/user/register", payload);
    }
}
