package com.zccenter.sdk;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoTest {

    private final Crypto crypto = new Crypto(new com.fasterxml.jackson.databind.ObjectMapper());

    @Test
    void canonicalRequestMatchesThinkPhpFixture() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("z", "最后");
        query.put("a", "first value");

        String canonical = crypto.canonicalRequest(
                "post",
                "/sapi/ping",
                query,
                "1787823238",
                "g3N4RKzB9QMHYVqE4-ug_SDl",
                "{\"echo\":\"hello\"}"
        );

        String expected = "POST\n/sapi/ping\na=first%20value&z=%E6%9C%80%E5%90%8E\n1787823238\ng3N4RKzB9QMHYVqE4-ug_SDl\n"
                + crypto.sha256Hex("{\"echo\":\"hello\"}");
        assertEquals(expected, canonical);
    }

    @Test
    void aesGcmRoundTrip() {
        String secret = "sdk-test-secret";
        String aad = crypto.aad("sdk-test-app-key", "1787823238", "g3N4RKzB9QMHYVqE4-ug_SDl");
        Map<String, Object> payload = Map.of("hello", "世界");

        Map<String, Object> envelope = crypto.encrypt(payload, secret, aad);
        Map<String, Object> decrypted = crypto.decrypt(envelope, secret, aad);
        assertEquals(payload, decrypted);

        String signature = crypto.sign(String.join("\n",
                "1787823238",
                "g3N4RKzB9QMHYVqE4-ug_SDl",
                (String) envelope.get("iv"),
                (String) envelope.get("tag"),
                (String) envelope.get("ciphertext")
        ), secret);
        assertTrue(crypto.verifyEncryptedResponse(envelope, signature, "1787823238", "g3N4RKzB9QMHYVqE4-ug_SDl", secret));
    }
}
