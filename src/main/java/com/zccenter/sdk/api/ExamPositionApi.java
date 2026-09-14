package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 招考岗位 SAPI。 */
public class ExamPositionApi extends AbstractApi {

    public ExamPositionApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/exam-position/list", params == null ? Map.of() : params);
    }

    public SapiResponse reportBatch(String noticeUuid, List<Map<String, Object>> items) {
        return reportBatch(noticeUuid, items, false);
    }

    public SapiResponse reportBatch(String noticeUuid, List<Map<String, Object>> items, boolean syncIndex) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notice_uuid", noticeUuid);
        payload.put("items", items == null ? List.of() : items);
        payload.put("sync_index", syncIndex);
        return post("/sapi/exam-position/report-batch", payload);
    }
}
