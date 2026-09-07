package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目 SAPI。
 *
 * @see docs/sapi/题库.md
 */
public class QuestionApi extends AbstractApi {

    public static final int TYPE_SINGLE = 1;
    public static final int TYPE_MULTI = 2;
    public static final int TYPE_JUDGE = 3;
    public static final int TYPE_BLANK = 4;
    public static final int TYPE_ESSAY = 5;

    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;

    public QuestionApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/question/list", params == null ? Map.of() : params);
    }

    public SapiResponse search(Map<String, Object> params) {
        return post("/sapi/question/search", params == null ? Map.of() : params);
    }

    public SapiResponse detail(String uuid, boolean includeAnswer) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uuid", uuid);
        payload.put("include_answer", includeAnswer);
        return post("/sapi/question/detail", payload);
    }

    public SapiResponse batch(List<String> uuids, boolean includeAnswer) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uuids", uuids == null ? List.of() : new ArrayList<>(uuids));
        payload.put("include_answer", includeAnswer);
        return post("/sapi/question/batch", payload);
    }

    /**
     * 向当前应用归属的题库上报题目。
     */
    public SapiResponse report(Map<String, Object> payload) {
        Map<String, Object> body = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        Map<String, Object> override = new LinkedHashMap<>();
        if (body.get("bank_uuid") != null) {
            override.put("bank_uuid", body.get("bank_uuid"));
        }
        if (body.get("bank_code") != null) {
            override.put("bank_code", body.get("bank_code"));
        }
        Map<String, Object> bank = client.resolveReportBank(override);
        body.remove("bank_uuid");
        body.remove("bank_code");
        Map<String, Object> merged = new LinkedHashMap<>(bank);
        merged.putAll(body);
        return post("/sapi/question/report", merged);
    }

    /**
     * 批量上报题目。
     */
    public SapiResponse reportBatch(List<Map<String, Object>> items, Map<String, Object> bank) {
        Map<String, Object> payload = new LinkedHashMap<>(client.resolveReportBank(bank));
        payload.put("items", items == null ? List.of() : items);
        return post("/sapi/question/report-batch", payload);
    }

    public SapiResponse reportBatch(List<Map<String, Object>> items) {
        return reportBatch(items, Map.of());
    }
}
