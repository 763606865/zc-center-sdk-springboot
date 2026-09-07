package com.zccenter.sdk.exception;

/**
 * 网络传输或响应解析失败。
 */
public class TransportException extends SapiException {

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
