package com.zgamelogic.data.metra;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MetraStop {
    @JsonProperty("stop_id")
    private String stopId;
    @JsonProperty("stop_name")
    private String stopName;
}
