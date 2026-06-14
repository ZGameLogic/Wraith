package com.zgamelogic.devops.dto.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublishVersionPayload(@JsonProperty("tag_name") String tagName, String name) {
}
