package com.zgamelogic.metra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetraTrip {
    @JsonProperty("trip_id")
    private String tripId;
    @JsonProperty("route_id")
    private String routeId;
    @JsonProperty("service_id")
    private String serviceId;
    @JsonProperty("trip_headsign")
    private String tripHeadsign;
    @JsonProperty("direction_id")
    private int directionId;
}
