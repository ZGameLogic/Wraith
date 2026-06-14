package com.zgamelogic.devops.dto.payloads;

public record TagPayload(
        String tag,
        String message,
        String object,
        String type
) {}
