package com.zccenter.sdk.exception;

/**
 * ZC Center SAPI SDK 基础异常。
 */
public class SapiException extends RuntimeException {

    public SapiException(String message) {
        super(message);
    }

    public SapiException(String message, Throwable cause) {
        super(message, cause);
    }
}
