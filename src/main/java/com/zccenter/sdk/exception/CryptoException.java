package com.zccenter.sdk.exception;

/**
 * 加解密失败。
 */
public class CryptoException extends SapiException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
