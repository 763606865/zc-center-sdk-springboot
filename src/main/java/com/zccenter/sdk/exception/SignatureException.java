package com.zccenter.sdk.exception;

/**
 * 请求或响应签名校验失败。
 */
public class SignatureException extends SapiException {

    public SignatureException(String message) {
        super(message);
    }
}
