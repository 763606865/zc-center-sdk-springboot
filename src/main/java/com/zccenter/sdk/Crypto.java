package com.zccenter.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zccenter.sdk.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * SAPI 签名与 AES-256-GCM 加解密，协议与中台 / ThinkPHP SDK 保持一致。
 */
public final class Crypto {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;

    public Crypto(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> encrypt(Map<String, Object> payload, String appSecret, String aad) {
        try {
            byte[] plaintext = jsonEncode(payload).getBytes(StandardCharsets.UTF_8);
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(appSecret), new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(plaintext);

            int cipherLen = encrypted.length - GCM_TAG_LENGTH;
            byte[] ciphertext = new byte[cipherLen];
            byte[] tag = new byte[GCM_TAG_LENGTH];
            System.arraycopy(encrypted, 0, ciphertext, 0, cipherLen);
            System.arraycopy(encrypted, cipherLen, tag, 0, GCM_TAG_LENGTH);

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("algorithm", "AES-256-GCM");
            envelope.put("ciphertext", Base64.getEncoder().encodeToString(ciphertext));
            envelope.put("iv", Base64.getEncoder().encodeToString(iv));
            envelope.put("tag", Base64.getEncoder().encodeToString(tag));
            return envelope;
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("SAPI请求数据加密失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decrypt(Map<String, Object> envelope, String appSecret, String aad) {
        try {
            String ciphertextB64 = asNonEmptyString(envelope.get("ciphertext"), "ciphertext");
            String ivB64 = asNonEmptyString(envelope.get("iv"), "iv");
            String tagB64 = asNonEmptyString(envelope.get("tag"), "tag");

            byte[] ciphertext = Base64.getDecoder().decode(ciphertextB64);
            byte[] iv = Base64.getDecoder().decode(ivB64);
            byte[] tag = Base64.getDecoder().decode(tagB64);
            if (iv.length != GCM_IV_LENGTH || tag.length != GCM_TAG_LENGTH) {
                throw new CryptoException("SAPI加密响应格式错误");
            }

            byte[] combined = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(appSecret), new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            byte[] plaintext = cipher.doFinal(combined);

            Object decoded = objectMapper.readValue(plaintext, Object.class);
            if (!(decoded instanceof Map<?, ?> map)) {
                throw new CryptoException("SAPI解密响应不是JSON对象");
            }
            return (Map<String, Object>) map;
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("SAPI响应解密或完整性验证失败", e);
        }
    }

    public String canonicalRequest(
            String method,
            String path,
            Map<String, ?> query,
            String timestamp,
            String nonce,
            String rawBody
    ) {
        String normalizedPath = "/" + path.replaceFirst("^/+", "");
        return String.join("\n",
                method.toUpperCase(Locale.ROOT),
                normalizedPath,
                canonicalQuery(query),
                timestamp,
                nonce,
                sha256Hex(rawBody)
        );
    }

    public String sign(String canonical, String appSecret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new CryptoException("SAPI签名计算失败", e);
        }
    }

    public String aad(String appKey, String timestamp, String nonce) {
        return String.join("\n", appKey, timestamp, nonce);
    }

    public boolean verifyEncryptedResponse(
            Map<String, Object> envelope,
            String signature,
            String timestamp,
            String nonce,
            String appSecret
    ) {
        Object iv = envelope.get("iv");
        Object tag = envelope.get("tag");
        Object ciphertext = envelope.get("ciphertext");
        if (!(iv instanceof String) || !(tag instanceof String) || !(ciphertext instanceof String)) {
            return false;
        }
        String canonical = String.join("\n", timestamp, nonce, (String) iv, (String) tag, (String) ciphertext);
        return !signature.isEmpty()
                && constantTimeEquals(sign(canonical, appSecret), signature.toLowerCase(Locale.ROOT));
    }

    public boolean verifyPlaintextResponse(
            String rawBody,
            String signature,
            String timestamp,
            String nonce,
            String appSecret
    ) {
        String canonical = String.join("\n", timestamp, nonce, sha256Hex(rawBody));
        return !signature.isEmpty()
                && constantTimeEquals(sign(canonical, appSecret), signature.toLowerCase(Locale.ROOT));
    }

    public String jsonEncode(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CryptoException("SAPI请求JSON编码失败", e);
        }
    }

    public Map<String, Object> jsonDecodeObject(String raw) {
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new CryptoException("SAPI响应JSON解码失败", e);
        }
    }

    public String nonce() {
        byte[] bytes = new byte[18];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new CryptoException("SHA-256计算失败", e);
        }
    }

    private SecretKey deriveKey(String appSecret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(appSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private String canonicalQuery(Map<String, ?> query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, ?> entry : query.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            sorted.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            parts.add(rfc3986Encode(entry.getKey()) + "=" + rfc3986Encode(entry.getValue()));
        }
        return String.join("&", parts);
    }

    private String rfc3986Encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private String asNonEmptyString(Object value, String field) {
        if (!(value instanceof String text) || text.isEmpty()) {
            throw new CryptoException("SAPI加密响应缺少字段：" + field);
        }
        return text;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
