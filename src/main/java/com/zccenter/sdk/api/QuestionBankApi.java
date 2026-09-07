package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/**
 * 题库 SAPI。
 *
 * @see docs/sapi/题库.md
 */
public class QuestionBankApi extends AbstractApi {

    public QuestionBankApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/question-bank/list", params == null ? Map.of() : params);
    }

    public SapiResponse detail(String uuid) {
        return post("/sapi/question-bank/detail", Map.of("uuid", uuid));
    }
}
