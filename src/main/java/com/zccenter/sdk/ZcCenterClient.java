package com.zccenter.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zccenter.sdk.api.AbstractApi;
import com.zccenter.sdk.api.AuthApi;
import com.zccenter.sdk.api.DictApi;
import com.zccenter.sdk.api.EnterpriseApi;
import com.zccenter.sdk.api.ExamNoticeApi;
import com.zccenter.sdk.api.PingApi;
import com.zccenter.sdk.api.QuestionApi;
import com.zccenter.sdk.api.QuestionBankApi;
import com.zccenter.sdk.api.ResumeApi;
import com.zccenter.sdk.api.UserApi;
import com.zccenter.sdk.exception.ApiException;
import com.zccenter.sdk.exception.SapiException;
import com.zccenter.sdk.exception.SignatureException;
import com.zccenter.sdk.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZC Center SAPI 客户端：统一处理签名、加解密、验签与业务接口入口。
 */
public final class ZcCenterClient {

    private static final Logger log = LoggerFactory.getLogger(ZcCenterClient.class);

    private final String baseUrl;
    private final String appKey;
    private final String appSecret;
    private final boolean encryption;
    private final boolean debug;
    private final boolean reportEnabled;
    private final String reportBankUuid;
    private final String reportBankCode;
    private final long timeoutMillis;
    private final HttpClient httpClient;
    private final Crypto crypto;
    private final ObjectMapper objectMapper;
    private final Map<Class<? extends AbstractApi>, AbstractApi> apis = new ConcurrentHashMap<>();

    public ZcCenterClient(ZcCenterProperties properties) {
        this(properties, null, null);
    }

    public ZcCenterClient(ZcCenterProperties properties, HttpClient httpClient, ObjectMapper objectMapper) {
        Objects.requireNonNull(properties, "properties");
        this.baseUrl = trimTrailingSlash(safeTrim(properties.getBaseUrl()));
        this.appKey = safeTrim(properties.getAppKey());
        this.appSecret = properties.getAppSecret() == null ? "" : properties.getAppSecret();
        this.encryption = properties.isEncryption();
        this.debug = properties.isDebug();
        this.reportEnabled = properties.isReportEnabled();
        this.reportBankUuid = safeTrim(properties.getReportBankUuid()).toLowerCase(Locale.ROOT);
        this.reportBankCode = safeTrim(properties.getReportBankCode());
        this.timeoutMillis = Math.max(1000L, (long) (properties.getTimeoutSeconds() * 1000));
        this.objectMapper = objectMapper == null ? defaultObjectMapper() : objectMapper;
        this.crypto = new Crypto(this.objectMapper);

        if (this.baseUrl.isEmpty() || !looksLikeUrl(this.baseUrl)) {
            throw new SapiException("ZC Center SDK的base_url配置无效");
        }
        if (this.appKey.isEmpty() || this.appSecret.isEmpty()) {
            throw new SapiException("ZC Center SDK的app_key或app_secret未配置");
        }
        if (this.reportEnabled && this.reportBankUuid.isEmpty() && this.reportBankCode.isEmpty()) {
            throw new SapiException("开启题目上报时必须配置 report_bank_uuid 或 report_bank_code");
        }

        this.httpClient = httpClient == null
                ? buildHttpClient(properties)
                : httpClient;
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    /**
     * 解析上报目标题库：调用方显式传入优先，否则使用配置。
     */
    public Map<String, Object> resolveReportBank(Map<String, Object> override) {
        if (!reportEnabled) {
            throw new SapiException("题目上报未开启，请在配置中设置 zc-center.report-enabled=true");
        }
        Map<String, Object> safeOverride = override == null ? Map.of() : override;
        String overrideUuid = safeTrim(stringValue(safeOverride.get("bank_uuid"))).toLowerCase(Locale.ROOT);
        String overrideCode = safeTrim(stringValue(safeOverride.get("bank_code")));
        boolean hasOverride = !overrideUuid.isEmpty() || !overrideCode.isEmpty();

        String uuid = hasOverride ? overrideUuid : reportBankUuid;
        String code = hasOverride ? overrideCode : reportBankCode;
        if (uuid.isEmpty() && code.isEmpty()) {
            throw new SapiException("请配置或传入上报目标题库 bank_uuid / bank_code");
        }

        Map<String, Object> bank = new LinkedHashMap<>();
        if (!uuid.isEmpty()) {
            bank.put("bank_uuid", uuid);
        }
        if (!code.isEmpty()) {
            bank.put("bank_code", code);
        }
        return bank;
    }

    public PingApi ping() {
        return api(PingApi.class);
    }

    public AuthApi auth() {
        return api(AuthApi.class);
    }

    public UserApi user() {
        return api(UserApi.class);
    }

    public EnterpriseApi enterprise() {
        return api(EnterpriseApi.class);
    }

    public ExamNoticeApi examNotice() {
        return api(ExamNoticeApi.class);
    }

    public ResumeApi resume() {
        return api(ResumeApi.class);
    }

    public DictApi dict() {
        return api(DictApi.class);
    }

    public QuestionApi question() {
        return api(QuestionApi.class);
    }

    public QuestionBankApi questionBank() {
        return api(QuestionBankApi.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractApi> T api(Class<T> apiClass) {
        return (T) apis.computeIfAbsent(apiClass, type -> {
            try {
                return type.getDeclaredConstructor(ZcCenterClient.class).newInstance(this);
            } catch (Exception e) {
                throw new SapiException("无法创建接口集合：" + type.getName(), e);
            }
        });
    }

    public SapiResponse get(String path, Map<String, Object> query) {
        return request("GET", path, Map.of(), query == null ? Map.of() : query);
    }

    public SapiResponse post(String path, Map<String, Object> payload) {
        return request("POST", path, payload == null ? Map.of() : payload, Map.of());
    }

    public SapiResponse request(String method, String path, Map<String, Object> payload, Map<String, Object> query) {
        String normalizedPath = "/" + path.replaceFirst("^/+", "");
        if (!normalizedPath.startsWith("/sapi/")) {
            throw new SapiException("SAPI请求路径必须以/sapi/开头");
        }

        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = crypto.nonce();
        String aad = crypto.aad(appKey, timestamp, nonce);

        Map<String, Object> safePayload = payload == null ? Map.of() : payload;
        Map<String, Object> safeQuery = query == null ? Map.of() : query;
        String body = crypto.jsonEncode(encryption ? crypto.encrypt(safePayload, appSecret, aad) : safePayload);
        String canonical = crypto.canonicalRequest(method, normalizedPath, safeQuery, timestamp, nonce, body);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("X-App-Key", appKey);
        headers.put("X-Timestamp", timestamp);
        headers.put("X-Nonce", nonce);
        headers.put("X-Signature", crypto.sign(canonical, appSecret));
        headers.put("X-Encrypted", encryption ? "1" : "0");

        URI uri = buildUri(normalizedPath, safeQuery);
        writeLog("SAPI request", Map.of(
                "method", method.toUpperCase(Locale.ROOT),
                "url", uri.toString(),
                "query", safeQuery,
                "request_headers", headers,
                "request_params", safePayload,
                "request_body", decodeJsonOrRaw(body)
        ));

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .method(method.toUpperCase(Locale.ROOT), HttpRequest.BodyPublishers.ofString(body));
            headers.forEach(builder::header);

            HttpResponse<String> httpResponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return parseResponse(httpResponse, timestamp, nonce, aad);
        } catch (SapiException e) {
            throw e;
        } catch (Exception e) {
            writeLog("SAPI request transport error", Map.of(
                    "method", method.toUpperCase(Locale.ROOT),
                    "url", uri.toString(),
                    "error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()
            ));
            throw new TransportException("SAPI网络请求失败：" + e.getMessage(), e);
        }
    }

    private SapiResponse parseResponse(HttpResponse<String> response, String timestamp, String nonce, String aad) {
        String rawBody = response.body() == null ? "" : response.body();
        int statusCode = response.statusCode();
        Map<String, String> responseHeaders = flattenHeaders(response.headers().map());
        String signature = response.headers().firstValue("X-Response-Signature").orElse("").trim();
        boolean encrypted = response.headers().firstValue("X-Encrypted").map("1"::equals).orElse(false);

        writeLog("SAPI response", Map.of(
                "http_status", statusCode,
                "response_headers", responseHeaders,
                "response_body", decodeJsonOrRaw(rawBody)
        ));

        Map<String, Object> decoded;
        try {
            decoded = crypto.jsonDecodeObject(rawBody);
        } catch (Exception e) {
            throw new TransportException("SAPI响应不是有效JSON", e);
        }

        if (signature.isEmpty()) {
            if (statusCode >= 400) {
                throw apiException(decoded, statusCode, false);
            }
            throw new SignatureException("SAPI响应缺少X-Response-Signature");
        }

        Map<String, Object> payload;
        if (encrypted) {
            if (!crypto.verifyEncryptedResponse(decoded, signature, timestamp, nonce, appSecret)) {
                throw new SignatureException("SAPI加密响应签名验证失败");
            }
            payload = crypto.decrypt(decoded, appSecret, aad);
        } else {
            if (!crypto.verifyPlaintextResponse(rawBody, signature, timestamp, nonce, appSecret)) {
                throw new SignatureException("SAPI明文响应签名验证失败");
            }
            payload = decoded;
        }

        writeLog("SAPI response payload", Map.of(
                "http_status", statusCode,
                "response_payload", payload
        ));

        int businessCode = toInt(payload.get("code"), 0);
        if (statusCode >= 400 || businessCode != 0) {
            throw apiException(payload, statusCode, true);
        }

        return new SapiResponse(statusCode, payload, responseHeaders, rawBody);
    }

    @SuppressWarnings("unchecked")
    private ApiException apiException(Map<String, Object> payload, int httpStatus, boolean signatureVerified) {
        String message = stringValue(payload.get("msg"));
        if (message.isEmpty()) {
            message = "SAPI接口调用失败";
        }
        Object data = payload.get("data");
        Map<String, Object> responseData = data instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
        return new ApiException(message, toInt(payload.get("code"), 0), httpStatus, responseData, signatureVerified);
    }

    private URI buildUri(String path, Map<String, Object> query) {
        StringBuilder sb = new StringBuilder(baseUrl).append(path);
        String q = buildQueryString(query);
        if (!q.isEmpty()) {
            sb.append('?').append(q);
        }
        return URI.create(sb.toString());
    }

    private String buildQueryString(Map<String, Object> query) {
        String canonical = crypto.canonicalRequest("GET", "/x", query, "t", "n", "");
        String[] lines = canonical.split("\n", -1);
        return lines.length >= 3 ? lines[2] : "";
    }

    private HttpClient buildHttpClient(ZcCenterProperties properties) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000L, (long) (properties.getConnectTimeoutSeconds() * 1000))));
        if (!properties.isVerifySsl()) {
            try {
                TrustManager[] trustAll = new TrustManager[]{
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAll, new java.security.SecureRandom());
                builder.sslContext(sslContext);
            } catch (Exception e) {
                throw new SapiException("无法关闭 SSL 校验", e);
            }
        }
        return builder.build();
    }

    private void writeLog(String message, Map<String, Object> context) {
        if (!debug) {
            return;
        }
        log.info("[ZcCenter SDK] {} {}", message, crypto.jsonEncode(context));
    }

    private Object decodeJsonOrRaw(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        try {
            return crypto.jsonDecodeObject(raw);
        } catch (Exception e) {
            return raw;
        }
    }

    private Map<String, String> flattenHeaders(Map<String, java.util.List<String>> headers) {
        Map<String, String> flat = new HashMap<>();
        headers.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                flat.put(k, v.get(0));
            }
        });
        return flat;
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

    private static boolean looksLikeUrl(String value) {
        try {
            URI uri = URI.create(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int toInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
