package com.zgamelogic.data.api.github.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class LabelsPayload {
    private List<String> labels;
}
