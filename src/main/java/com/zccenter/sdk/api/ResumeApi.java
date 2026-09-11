package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/** 简历同步 SAPI。 */
public class ResumeApi extends AbstractApi {
    public ResumeApi(ZcCenterClient client) { super(client); }
    public SapiResponse list(Map<String, Object> params) { return post("/sapi/resume/list", params == null ? Map.of() : params); }
    public SapiResponse detail(String uuid) { return post("/sapi/resume/detail", Map.of("uuid", uuid)); }
    public SapiResponse report(Map<String, Object> resume) { return post("/sapi/resume/report", resume == null ? Map.of() : resume); }
    public SapiResponse update(Map<String, Object> resume) { return post("/sapi/resume/update", resume == null ? Map.of() : resume); }
}
