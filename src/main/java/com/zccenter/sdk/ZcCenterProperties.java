package com.zccenter.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ZC Center SAPI 客户端配置。
 */
@ConfigurationProperties(prefix = "zc-center")
public class ZcCenterProperties {

    /**
     * 中台根地址，例如 https://zc-center.example.com
     */
    private String baseUrl = "";

    private String appKey = "";

    private String appSecret = "";

    /**
     * 是否启用 AES-GCM；须与中台 SAPI_ENCRYPTION_ENABLED 一致。
     */
    private boolean encryption = true;

    private double timeoutSeconds = 10;

    private double connectTimeoutSeconds = 3;

    private boolean verifySsl = true;

    private boolean debug = false;

    /**
     * 是否开启题目上报。
     */
    private boolean reportEnabled = false;

    private String reportBankUuid = "";

    private String reportBankCode = "";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isEncryption() {
        return encryption;
    }

    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    public double getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(double timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public double getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(double connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public boolean isVerifySsl() {
        return verifySsl;
    }

    public void setVerifySsl(boolean verifySsl) {
        this.verifySsl = verifySsl;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public String getReportBankUuid() {
        return reportBankUuid;
    }

    public void setReportBankUuid(String reportBankUuid) {
        this.reportBankUuid = reportBankUuid;
    }

    public String getReportBankCode() {
        return reportBankCode;
    }

    public void setReportBankCode(String reportBankCode) {
        this.reportBankCode = reportBankCode;
    }
}
