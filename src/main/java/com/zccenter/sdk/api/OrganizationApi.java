package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

/** 组织管理 SAPI。参见 docs/sapi/组织.md。 */
public class OrganizationApi extends AbstractApi {
    public static final String TYPE_ENTERPRISE = "enterprise";
    public static final String TYPE_SCHOOL = "school";
    public static final String TYPE_GOVERNMENT = "government";
    public static final String TYPE_PUBLIC_INSTITUTION = "public_institution";
    public static final String TYPE_ASSOCIATION = "association";
    public static final String TYPE_LAW_FIRM = "law_firm";
    public static final String TYPE_HR_AGENCY = "hr_agency";
    public static final String TYPE_MEDICAL = "medical";
    public static final String TYPE_FOUNDATION = "foundation";
    public static final String TYPE_COMMUNITY = "community";
    public static final String TYPE_OTHER = "other";

    public OrganizationApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse report(Map<String, Object> payload) {
        return post("/sapi/organization/report", payload);
    }

    public SapiResponse detail(Map<String, Object> payload) {
        return post("/sapi/organization/detail", payload);
    }
}
