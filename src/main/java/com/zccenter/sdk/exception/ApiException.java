package com.zccenter.sdk.exception;

import java.util.Map;

/**
 * SAPI 业务错误（HTTP 非 2xx 或业务 code != 0）。
 */
public class ApiException extends SapiException {

    private final int businessCode;
    private final int httpStatus;
    private final Map<String, Object> responseData;
    private final boolean signatureVerified;

    public ApiException(
            String message,
            int businessCode,
            int httpStatus,
            Map<String, Object> responseData,
            boolean signatureVerified
    ) {
        super(message);
        this.businessCode = businessCode;
        this.httpStatus = httpStatus;
        this.responseData = responseData;
        this.signatureVerified = signatureVerified;
    }

    public int getBusinessCode() {
        return businessCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }

    public boolean isSignatureVerified() {
        return signatureVerified;
    }
}
