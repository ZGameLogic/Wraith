package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetraStop {
    @JsonProperty("stop_id")
    private String stopId;
    @JsonProperty("stop_name")
    private String stopName;
}
