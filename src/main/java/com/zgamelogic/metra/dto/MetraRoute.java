package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MetraRoute {
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("route_long_name")
    private String routeLongName;
    @JsonProperty("route_color")
    private String routeColor;
}
