package com.zgamelogic.data.api.github.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssueCreatePayload {
    private String title;
    private String body;
}
