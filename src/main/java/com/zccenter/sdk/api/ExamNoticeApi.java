package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 招考公告 SAPI。内嵌 positions 会自动拆出并分批走岗位接口。 */
public class ExamNoticeApi extends AbstractApi {

    private static final int POSITION_BATCH = 100;

    public ExamNoticeApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse list(Map<String, Object> params) {
        return post("/sapi/exam-notice/list", params == null ? Map.of() : params);
    }

    public SapiResponse report(Map<String, Object> notice) {
        Map<String, Object> payload = copy(notice);
        List<Map<String, Object>> positions = detachPositions(payload);
        SapiResponse response = post("/sapi/exam-notice/report", payload);
        String uuid = noticeUuid(response.getDataAsMap().get("notice"));
        if (uuid.isBlank()) {
            uuid = stringValue(payload.get("uuid"));
        }
        pushPositions(uuid, positions);
        return response;
    }

    public SapiResponse reportBatch(List<Map<String, Object>> items) {
        List<Map<String, Object>> stripped = new ArrayList<>();
        List<List<Map<String, Object>>> pending = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                Map<String, Object> payload = copy(item);
                pending.add(detachPositions(payload));
                stripped.add(payload);
            }
        }
        SapiResponse response = post("/sapi/exam-notice/report-batch", Map.of("items", stripped));
        Object data = response.getData();
        if (!(data instanceof Map<?, ?> dataMap)) {
            return response;
        }
        Object resultsObj = dataMap.get("results");
        if (!(resultsObj instanceof List<?> results)) {
            return response;
        }
        for (int i = 0; i < results.size(); i++) {
            Object rowObj = results.get(i);
            if (!(rowObj instanceof Map<?, ?> row)) {
                continue;
            }
            if ("failed".equals(String.valueOf(row.get("action")))) {
                continue;
            }
            String uuid = noticeUuid(row.get("notice"));
            if (uuid.isBlank() && i < stripped.size()) {
                uuid = stringValue(stripped.get(i).get("uuid"));
            }
            pushPositions(uuid, i < pending.size() ? pending.get(i) : List.of());
        }
        return response;
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        return source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> detachPositions(Map<String, Object> notice) {
        Object raw = notice.remove("positions");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> positions = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                positions.add((Map<String, Object>) map);
            }
        }
        return positions;
    }

    private String noticeUuid(Object notice) {
        if (notice instanceof Map<?, ?> map) {
            return stringValue(map.get("uuid"));
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void pushPositions(String noticeUuid, List<Map<String, Object>> positions) {
        if (noticeUuid == null || noticeUuid.isBlank() || positions == null || positions.isEmpty()) {
            return;
        }
        int lastStart = ((positions.size() - 1) / POSITION_BATCH) * POSITION_BATCH;
        for (int start = 0; start < positions.size(); start += POSITION_BATCH) {
            int end = Math.min(start + POSITION_BATCH, positions.size());
            client.examPosition().reportBatch(
                    noticeUuid,
                    positions.subList(start, end),
                    start == lastStart
            );
        }
    }
}
