package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.List;
import java.util.Map;

/** 招考公告 SAPI。 */
public class ExamNoticeApi extends AbstractApi {

    public ExamNoticeApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/exam-notice/list", params == null ? Map.of() : params);
    }

    public SapiResponse report(Map<String, Object> notice) {
        return post("/sapi/exam-notice/report", notice == null ? Map.of() : notice);
    }

    public SapiResponse reportBatch(List<Map<String, Object>> items) {
        return post("/sapi/exam-notice/report-batch", Map.of("items", items == null ? List.of() : items));
    }
}
