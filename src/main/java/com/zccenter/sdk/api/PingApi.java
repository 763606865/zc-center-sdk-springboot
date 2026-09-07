package com.zccenter.sdk.api;

import com.zccenter.sdk.SapiResponse;
import com.zccenter.sdk.ZcCenterClient;

import java.util.Map;

public class PingApi extends AbstractApi {

    public PingApi(ZcCenterClient client) {
        super(client);
    }

    public SapiResponse send() {
        return send("hello");
    }

    public SapiResponse send(String echo) {
        return post("/sapi/ping", Map.of("echo", echo == null ? "hello" : echo));
    }
}
